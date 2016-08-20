package ch.vvingolds.xsdiff.app;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class XmlDomUtilsTest {

    @Test
    public void shouldRemoveNsPrefix() {
        assertThat( XmlDomUtils.removeNsPrefix( "xs:element" ), is( "element") );
    }

    @Test
    public void shouldHandleEdgeCasesOnNsRemove() {
        assertThat( XmlDomUtils.removeNsPrefix( "element" ), is( "element") );
        assertThat( XmlDomUtils.removeNsPrefix( ":element" ), is( "element") );
        assertThat( XmlDomUtils.removeNsPrefix( "xs:" ), is( "") );
        assertThat( XmlDomUtils.removeNsPrefix( ":element:" ), is( "element:") );
    }

    @Test
    public void shouldCountXpathDepth() {
        assertThat( XmlDomUtils.xpathDepth( null ), is( 0L ) );
        assertThat( XmlDomUtils.xpathDepth( "" ), is( 0L ) );
        assertThat( XmlDomUtils.xpathDepth( "/a" ), is( 1L ) );
        assertThat( XmlDomUtils.xpathDepth( "/a/b" ), is( 2L ) );
        assertThat( XmlDomUtils.xpathDepth( "/a/b/c" ), is( 3L ) );
    }

    @Test
    public void shouldCutXpathParent() {
        assertThat( XmlDomUtils.wideContext( null ), is( nullValue() ) );
        assertThat( XmlDomUtils.wideContext( "" ), is( "" ) );
        assertThat( XmlDomUtils.wideContext( "/a" ), is( "/a" ) ); // do not cut if already short
        assertThat( XmlDomUtils.wideContext( "//" ), is( "//" ) );
        assertThat( XmlDomUtils.wideContext( "/a/b" ), is( "/a/b"  ) );
        assertThat( XmlDomUtils.wideContext( "/a/b/c" ), is( "/a/b"  ) );
        assertThat( XmlDomUtils.wideContext( "a//b/c" ), is( "a//b"  ) );
        assertThat( XmlDomUtils.wideContext( "a//b/c///d" ), is( "a//b"  ) );
        assertThat( XmlDomUtils.wideContext( "a//b/c/d/e" ), is( "a//b"  ) );
        assertThat( XmlDomUtils.wideContext( "/a/b//c//d//e" ), is( "/a/b"  ) );
    }

}
