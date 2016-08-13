package ch.vvingolds.xsdiff.format;

/** high level interface to produce colored diff output into content stream */
public interface DiffOutput {

    public void clearPart( final String text );

    public void removedPart( final String text );

    public void addedPart( final String text );

    void newline();
}
