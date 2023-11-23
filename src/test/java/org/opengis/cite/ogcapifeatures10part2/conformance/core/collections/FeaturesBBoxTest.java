package org.opengis.cite.ogcapifeatures10part2.conformance.core.collections;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10part2.conformance.RequirementClass;
import org.opengis.cite.ogcapifeatures10part2.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10part2.openapi3.TestPoint;
import org.opengis.cite.ogcapifeatures10part2.util.BBox;
import org.testng.ISuite;
import org.testng.ITestContext;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeaturesBBoxTest {

    private static ITestContext testContext;

    private static ISuite suite;

    private static TestPoint testPoint;

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        OpenApi3Parser parser = new OpenApi3Parser();
        URL openAppiDocument = FeaturesBBoxTest.class.getResource( "../../../openapi3/openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );

        InputStream json = FeaturesBBoxTest.class.getResourceAsStream( "../collections/collections.json" );
        JsonPath collectionsResponse = new JsonPath( json );
        List<Map<String, Object>> collections = collectionsResponse.getList( "collections" );

        List<RequirementClass> requirementClasses = new ArrayList();
        requirementClasses.add( RequirementClass.CORE );

        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        testPoint = new TestPoint( "http://localhost:8090/rest/services/kataster", "/collections/flurstueck/items",
                                   Collections.emptyMap() );

        URI landingPageUri = new URI( "https://www.ldproxy.nrw.de/kataster" );
        when( suite.getAttribute( SuiteAttribute.IUT.getName() ) ).thenReturn( landingPageUri );
        when( suite.getAttribute( SuiteAttribute.API_MODEL.getName() ) ).thenReturn( apiModel );
        when( suite.getAttribute( SuiteAttribute.COLLECTIONS.getName() ) ).thenReturn( collections );
        when( suite.getAttribute( SuiteAttribute.REQUIREMENTCLASSES.getName() ) ).thenReturn( requirementClasses );
    }

    @Before
    public void setUp() {
        initJadlerListeningOn( 8090 );
    }

    @After
    public void tearDown() {
        closeJadler();
    }

    @Test
    public void testParameterDefinition() {
        prepareJadler();
        FeaturesBBox features = initFeaturesBbox();

        features.boundingBoxParameterDefinition( testPoint );
    }

    @Test
    public void test() {
        prepareJadler();
        FeaturesBBox features = initFeaturesBbox();

        Map<String, Object> collection = prepareCollection();
        BBox bbox = new BBox( 5, 48, 9, 50 );
        features.validateFeaturesWithBoundingBoxOperation( collection, bbox );
        features.validateFeaturesWithBoundingBoxResponse_TypeProperty( collection, bbox );
        features.validateFeaturesWithBoundingBoxResponse_FeaturesProperty( collection, bbox );
        features.validateFeaturesWithBoundingBoxResponse_Links( collection, bbox );
        // skipped (collection missing):
        // features.validateFeaturesWithBoundingBoxResponse_TimeStamp( collection, bbox );
        // skipped (collection missing):
        // features.validateFeaturesWithBoundingBoxResponse_NumberMatched( collection , bbox );
        // skipped (collection missing):
        // features.validateFeaturesResponse_NumberReturned( collection, bbox );
    }

    private FeaturesBBox initFeaturesBbox() {
        FeaturesBBox features = new FeaturesBBox();
        features.initCommonFixture( testContext );
        features.retrieveRequiredInformationFromTestContext( testContext );
        features.requirementClasses( testContext );
        features.retrieveApiModel( testContext );
        return features;
    }

    private static Map<String, Object> prepareCollection() {
        return new JsonPath( FeatureCollectionTest.class.getResourceAsStream( "collection-flurstueck.json" ) ).get();
    }

    private void prepareJadler() {
        InputStream flurstueckItems = getClass().getResourceAsStream( "collectionItems-flurstueck.json" );
        onRequest().havingPath( endsWith( "collections/flurstueck/items" ) ).havingParameter( "bbox" ).respond().withBody( flurstueckItems );
    }

}
