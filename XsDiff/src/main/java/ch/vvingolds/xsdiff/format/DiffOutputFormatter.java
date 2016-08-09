package ch.vvingolds.xsdiff.format;

/** formatter provides content simply to DiffOutput consumer */
public interface DiffOutputFormatter {

    void printDiff( final DiffOutput output );
}
