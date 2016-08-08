package ch.vvingolds.xsdiff.format;

public interface DiffOutput {

    public void clearPart( final String text );

    public void removedPart( final String text );

    public void addedPart( final String text );

}
