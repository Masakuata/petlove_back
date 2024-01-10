package xatal.petlove.structures;

public enum MIMEType {
    TEXT_CSV("text/csv"),
    APPLICATION_JSON("application/json");

    private final String text;

    MIMEType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
