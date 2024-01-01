package xatal.sharedz.structures;

public class Attachment {
    private String filename;
    private byte[] bytes;
    private String mimeType;

    public Attachment() {
    }

    public Attachment(String filename, byte[] bytes, String mimeType) {
        this.filename = filename;
        this.bytes = bytes;
        this.mimeType = mimeType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
