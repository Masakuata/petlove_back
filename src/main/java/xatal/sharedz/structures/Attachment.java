package xatal.sharedz.structures;

public class Attachment {
    private String filename;
    private byte[] bytes;
    private MIMEType mimeType;

    public Attachment() {
    }

    public Attachment(String filename, byte[] bytes, MIMEType mimeType) {
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

    public MIMEType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MIMEType mimeType) {
        this.mimeType = mimeType;
    }
}
