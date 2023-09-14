package nettunit.MUSA;

public enum StateOfWorldUpdateOp {
    ADD("ADD"),
    REMOVE("REMOVE");

    private final String text;

    /**
     * @param text
     */
    StateOfWorldUpdateOp(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
