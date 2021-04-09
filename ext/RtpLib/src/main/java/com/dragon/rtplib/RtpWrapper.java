package com.dragon.rtplib;

public class RtpWrapper {

    private long nativeObject = 0;
    private IDataCallback callback;

    public RtpWrapper() {
        init();
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    public void setCallback(IDataCallback callback) {
        this.callback = callback;
    }

    void receivedData(byte[] buffer, int len) {
        if(this.callback != null)
        this.callback.onReceivedData(buffer, len);
    }


    public interface IDataCallback {
        void onReceivedData(byte[] buffer, int len);
    }

    static {
        try {
            System.loadLibrary("rtp-lib");
            initLib();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private native static void initLib();

    private native boolean init();

    private native boolean release();

    public native boolean open(int port, int payloadType, int sampleRate);

    public native boolean close();

    /**
     * @param ip "192.168.1.1"
     * @return
     */
    public native boolean addDestinationIp(String ip);

    public native int sendData(byte[] buffer, int len, int payloadType, boolean mark, int increase);
}
