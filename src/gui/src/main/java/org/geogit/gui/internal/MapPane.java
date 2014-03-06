package org.geogit.gui.internal;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.swing.JFrame;

import org.geogit.api.GeoGIT;
import org.geogit.geotools.data.GeoGitDataStore;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Throwables;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class MapPane {
    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();

    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

    private GeoGitDataStore store;

    private MapContent context;

    private GeoGIT geogit;

    public MapPane(GeoGIT geogit) throws IOException {
        this(geogit, null);
    }

    public MapPane(GeoGIT geogit, @Nullable List<String> layerNames) throws IOException {
        this.geogit = geogit;
        store = new GeoGitDataStore(geogit);

        List<String> displayNames = Lists.newArrayList();
        if (layerNames != null && !layerNames.isEmpty()) {
            displayNames.addAll(layerNames);
        } else {
            displayNames.addAll(Arrays.asList(store.getTypeNames()));
        }
        context = new MapContent();

        ReferencedEnvelope mapBounds = null;
        
        for (String name : displayNames) {
            FeatureSource featureSource = store.getFeatureSource(name);
            System.err.printf("Getting bounds of %s\n", name);
            ReferencedEnvelope bounds;
            try {
                bounds = featureSource.getBounds();
            } catch (Exception e) {
                e.printStackTrace();
                throw Throwables.propagate(e);
            }
            System.err.printf("%s bounds: %s\n", name, bounds);
            Style style = createStyle2(featureSource);
            Layer layer = new FeatureLayer(featureSource, style);
            layer.setTitle(name);
            context.addLayer(layer);
        }
    }

    public void show() {
        System.err.printf("showing map\n");

        final JMapFrame frame = new JMapFrame(context);
        frame.enableStatusBar(true);
        frame.enableToolBar(true);
        frame.initComponents();
        frame.setSize(800, 600);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                store.dispose();
                geogit.close();
            }

        });
        frame.setVisible(true);
    }

    /**
     * Here is a programmatic alternative to using JSimpleStyleDialog to get a Style. This methods
     * works out what sort of feature geometry we have in the shapefile and then delegates to an
     * appropriate style creating method.
     */
    private Style createStyle2(FeatureSource featureSource) {
        SimpleFeatureType schema = (SimpleFeatureType) featureSource.getSchema();
        Class geomType = schema.getGeometryDescriptor().getType().getBinding();

        if (Polygon.class.isAssignableFrom(geomType)
                || MultiPolygon.class.isAssignableFrom(geomType)) {
            return createPolygonStyle();

        } else if (LineString.class.isAssignableFrom(geomType)
                || MultiLineString.class.isAssignableFrom(geomType)) {
            return createLineStyle();

        } else {
            return createPointStyle();
        }
    }

    /**
     * Create a Style to draw polygon features with a thin blue outline and a cyan fill
     */
    private Style createPolygonStyle() {

        // create a partially opaque outline stroke
        Stroke stroke = styleFactory.createStroke(filterFactory.literal(Color.BLUE),
                filterFactory.literal(1), filterFactory.literal(0.5));

        // create a partial opaque fill
        Fill fill = styleFactory.createFill(filterFactory.literal(Color.CYAN),
                filterFactory.literal(0.5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to draw the default
         * geomettry of features
         */
        PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke, fill, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[] { rule });
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    /**
     * Create a Style to draw line features as thin blue lines
     */
    private Style createLineStyle() {
        Stroke stroke = styleFactory.createStroke(filterFactory.literal(Color.BLUE),
                filterFactory.literal(1));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to draw the default
         * geomettry of features
         */
        LineSymbolizer sym = styleFactory.createLineSymbolizer(stroke, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[] { rule });
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    /**
     * Create a Style to draw point features as circles with blue outlines and cyan fill
     */
    private Style createPointStyle() {
        Graphic gr = styleFactory.createDefaultGraphic();

        Mark mark = styleFactory.getCircleMark();

        mark.setStroke(styleFactory.createStroke(filterFactory.literal(Color.BLUE),
                filterFactory.literal(1)));

        mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add(mark);
        gr.setSize(filterFactory.literal(5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to draw the default
         * geomettry of features
         */
        PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[] { rule });
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }
}
