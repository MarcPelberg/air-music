package airplaylib

import "testing"

type testHandler struct{}

func (testHandler) OnDeviceFound(string)    {}
func (testHandler) OnConnected()            {}
func (testHandler) OnDisconnected(string)   {}
func (testHandler) OnPinRequired()          {}
func (testHandler) OnError(string)          {}
func (testHandler) OnLog(string)            {}

func TestSendAudioPCMWithoutConnectedPipeIsSafe(t *testing.T) {
	session := NewSession(testHandler{})
	session.SendAudioPCM([]byte{0x01, 0x02})
}
