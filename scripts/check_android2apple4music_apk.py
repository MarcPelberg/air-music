#!/usr/bin/env python3
import argparse
import subprocess
import sys


ALLOWED_PERMISSIONS = {
    "android.permission.RECORD_AUDIO",
    "android.permission.MODIFY_AUDIO_SETTINGS",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION",
    "android.permission.INTERNET",
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.WAKE_LOCK",
    "com.marcpelberg.android2apple4music.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
}

FORBIDDEN_COMPONENT_MARKERS = (
    ".SunshineService",
    ".MirrorActivity",
    ".TouchscreenActivity",
    ".MirrorTouchscreenProvider",
    "androidx.core.content.FileProvider",
    "androidx.profileinstaller.ProfileInstallReceiver",
    "rikka.shizuku.ShizukuProvider",
    "moe.shizuku",
)

ALLOWED_EXPORTED_COMPONENTS = {
    ("activity", "io.github.jqssun.displaymirror.MirrorMainActivity"),
}


def aapt_dump(aapt2, apk, *args):
    return subprocess.check_output(
        [aapt2, "dump", *args, apk],
        text=True,
        stderr=subprocess.STDOUT,
    )


def extract_permissions(output):
    permissions = set()
    for line in output.splitlines():
        line = line.strip()
        if not line.startswith("uses-permission: name='"):
            continue
        permissions.add(line.split("'", 2)[1])
    return permissions


def _raw_value(line):
    marker = 'Raw: "'
    if marker in line:
        return line.split(marker, 1)[1].split('"', 1)[0]
    if '="' in line:
        return line.split('="', 1)[1].split('"', 1)[0]
    if "=true" in line:
        return "true"
    if "=false" in line:
        return "false"
    return None


def extract_components(manifest_output):
    components = []
    current = None
    component_tags = {"activity", "activity-alias", "service", "receiver", "provider"}

    for line in manifest_output.splitlines():
        stripped = line.strip()
        if stripped.startswith("E: "):
            tag = stripped.split()[1]
            if tag in component_tags:
                current = {"tag": tag, "indent": len(line) - len(line.lstrip()), "attrs": {}}
                components.append(current)
            elif current and len(line) - len(line.lstrip()) <= current["indent"]:
                current = None
            continue

        if not current or not stripped.startswith("A: "):
            continue

        indent = len(line) - len(line.lstrip())
        if indent != current["indent"] + 2:
            continue

        if ":name(" in stripped:
            current["attrs"]["name"] = _raw_value(stripped)
        elif ":exported(" in stripped:
            current["attrs"]["exported"] = _raw_value(stripped)
        elif ":permission(" in stripped:
            current["attrs"]["permission"] = _raw_value(stripped)

    return components


def extract_application_attrs(manifest_output):
    attrs = {}
    in_application = False
    application_indent = None

    for line in manifest_output.splitlines():
        stripped = line.strip()
        indent = len(line) - len(line.lstrip())
        if stripped.startswith("E: application"):
            in_application = True
            application_indent = indent
            continue
        if in_application and stripped.startswith("E: ") and indent <= application_indent:
            break
        if in_application and stripped.startswith("A: ") and indent == application_indent + 2:
            if ":allowBackup(" in stripped:
                attrs["allowBackup"] = _raw_value(stripped)
            elif ":debuggable(" in stripped:
                attrs["debuggable"] = _raw_value(stripped)
    return attrs


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--aapt2", required=True)
    parser.add_argument("--apk", required=True)
    args = parser.parse_args()

    permission_output = aapt_dump(args.aapt2, args.apk, "permissions")
    actual_permissions = extract_permissions(permission_output)
    unexpected = sorted(actual_permissions - ALLOWED_PERMISSIONS)
    missing = sorted(ALLOWED_PERMISSIONS - actual_permissions)

    manifest_output = aapt_dump(args.aapt2, args.apk, "xmltree", "--file", "AndroidManifest.xml")
    forbidden_components = [
        marker for marker in FORBIDDEN_COMPONENT_MARKERS if marker in manifest_output
    ]
    app_attrs = extract_application_attrs(manifest_output)
    components = extract_components(manifest_output)
    exported_components = sorted(
        (component["tag"], component["attrs"].get("name", "<unknown>"))
        for component in components
        if component["attrs"].get("exported") == "true"
    )
    unexpected_exported = [
        f"{tag}:{name}"
        for tag, name in exported_components
        if (tag, name) not in ALLOWED_EXPORTED_COMPONENTS
    ]
    missing_launcher = sorted(ALLOWED_EXPORTED_COMPONENTS - set(exported_components))

    failures = []
    if unexpected:
        failures.append("unexpected permissions: " + ", ".join(unexpected))
    if missing:
        failures.append("missing permissions: " + ", ".join(missing))
    if forbidden_components:
        failures.append("forbidden components: " + ", ".join(forbidden_components))
    if unexpected_exported:
        failures.append("unexpected exported components: " + ", ".join(unexpected_exported))
    if missing_launcher:
        failures.append(
            "missing exported launcher: "
            + ", ".join(f"{tag}:{name}" for tag, name in missing_launcher)
        )
    if app_attrs.get("allowBackup") != "false":
        failures.append("android:allowBackup must be false")
    if app_attrs.get("debuggable") == "true":
        failures.append("release APK must not be debuggable")

    if failures:
        for failure in failures:
            print(failure, file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
