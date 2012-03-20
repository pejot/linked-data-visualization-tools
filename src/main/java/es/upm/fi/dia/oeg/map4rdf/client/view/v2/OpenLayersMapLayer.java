/**
 * Copyright (c) 2011 Alexander De Leon Battista
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package es.upm.fi.dia.oeg.map4rdf.client.view.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.Size;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureSelectedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Geometry;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.LinearRing;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.popup.Popup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import es.upm.fi.dia.oeg.map4rdf.client.presenter.MapPresenter;
import es.upm.fi.dia.oeg.map4rdf.client.style.StyleMapShape;
import es.upm.fi.dia.oeg.map4rdf.share.Circle;
import es.upm.fi.dia.oeg.map4rdf.share.OpenLayersAdapter;
import es.upm.fi.dia.oeg.map4rdf.share.Point;
import es.upm.fi.dia.oeg.map4rdf.share.PointBean;
import es.upm.fi.dia.oeg.map4rdf.share.PolyLine;
import es.upm.fi.dia.oeg.map4rdf.share.Polygon;
import javax.mail.search.SizeTerm;
import org.junit.experimental.theories.PotentialAssignment;

/**
 * @author Alexander De Leon
 */
public class OpenLayersMapLayer implements MapLayer, VectorFeatureSelectedListener {

	private static final String MARKER_ICON = "marker_red.png";
	private static final int CIRCLE_NUMBER_OF_POINTS = 20;
	private final Vector vectorLayer;
        private final Vector vectorBckgLayer;
        
	private final Set<VectorFeature> features = new HashSet<VectorFeature>();
	private final OpenLayersMapView owner;
	private final Map map;
	private final java.util.Map<String, List<ClickHandler>> handlers = new HashMap<String, List<ClickHandler>>();

	public OpenLayersMapLayer(OpenLayersMapView owner, Map map, String name) {
		this.owner = owner;
		this.map = map;
		VectorOptions vectorOptions = new VectorOptions();
                VectorOptions vectorBckgOptions = new VectorOptions();
                
		vectorLayer = new Vector(name + "_vectors", vectorOptions);
                vectorLayer.setDisplayInLayerSwitcher(false);

                vectorBckgLayer = new Vector(name + "_bckg_vectors", vectorOptions);
                vectorBckgLayer.setDisplayInLayerSwitcher(false);
                
                map.addLayer(vectorLayer);
                map.addLayer(vectorBckgLayer);
	}
        
	@Override
	public HasClickHandlers draw(Point point) {
                LonLat ll = new LonLat(point.getX(), point.getY());
                ll.transform("EPSG:4326", "EPSG:3857");
             
                org.gwtopenmaps.openlayers.client.geometry.Point olPoint = new org.gwtopenmaps.openlayers.client.geometry.Point(
				ll.lon(),ll.lat());
		//Window.alert(new Double(olPoint.getY()).toString() + " " + new Double(olPoint.getX()).toString() );
                return addFeature(olPoint, getStyle(olPoint));
	}
        public HasClickHandlers drawFlat(Point point) {
                LonLat ll = new LonLat(point.getX(), point.getY());
                ll.transform("EPSG:4326","EPSG:4326" );
             
                org.gwtopenmaps.openlayers.client.geometry.Point olPoint = new org.gwtopenmaps.openlayers.client.geometry.Point(
				ll.lon(),ll.lat());
		//Window.alert(new Double(olPoint.getY()).toString() + " " + new Double(olPoint.getX()).toString() );
                return addFlatFeature(olPoint, getStyle(olPoint));
	}
        

	@Override
	public HasClickHandlers drawPolygon(StyleMapShape<Polygon> polygon) {
		LinearRing ring = new LinearRing(getPoints(polygon.getMapShape().getPoints()));
		return addFeature(new org.gwtopenmaps.openlayers.client.geometry.Polygon(new LinearRing[] { ring }),
				getStyle(polygon));
	}

	@Override
	public HasClickHandlers drawPolyline(StyleMapShape<PolyLine> polyline) {
		LineString lineString = new LineString(getPoints(polyline.getMapShape().getPoints()));
		return addFeature(lineString, getStyle(polyline));
	}

	@Override
	public HasClickHandlers drawCircle(StyleMapShape<Circle> circle) {
		org.gwtopenmaps.openlayers.client.geometry.Point[] circlePoints = new org.gwtopenmaps.openlayers.client.geometry.Point[CIRCLE_NUMBER_OF_POINTS];

		double EARTH_RADIUS = 6371000;
		double d = circle.getMapShape().getRadius() / EARTH_RADIUS;
		double lat1 = Math.toRadians(circle.getMapShape().getCenter().getY());
		double lng1 = Math.toRadians(circle.getMapShape().getCenter().getX());

		double a = 0;
		double step = 360.0 / CIRCLE_NUMBER_OF_POINTS;
		for (int i = 0; i < CIRCLE_NUMBER_OF_POINTS; i++) {
			double tc = Math.toRadians(a);
			double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d) + Math.cos(lat1) * Math.sin(d) * Math.cos(tc));
			double lng2 = lng1
					+ Math.atan2(Math.sin(tc) * Math.sin(d) * Math.cos(lat1),
							Math.cos(d) - Math.sin(lat1) * Math.sin(lat2));
			circlePoints[i] = new org.gwtopenmaps.openlayers.client.geometry.Point(Math.toDegrees(lng2),
					Math.toDegrees(lat2));
			a += step;
		}
		LinearRing ring = new LinearRing(circlePoints);
		return addFeature((new org.gwtopenmaps.openlayers.client.geometry.Polygon(new LinearRing[] { ring })),
				getStyle(circle));
	}

	@Override
	public HasClickHandlers drawCircle(StyleMapShape<Circle> circle, String text) {
		org.gwtopenmaps.openlayers.client.geometry.Point[] circlePoints = new org.gwtopenmaps.openlayers.client.geometry.Point[CIRCLE_NUMBER_OF_POINTS];

		double EARTH_RADIUS = 6371000;
		double d = circle.getMapShape().getRadius() / EARTH_RADIUS;
		double lat1 = Math.toRadians(circle.getMapShape().getCenter().getY());
		double lng1 = Math.toRadians(circle.getMapShape().getCenter().getX());

		double a = 0;
		double step = 360.0 / CIRCLE_NUMBER_OF_POINTS;
		for (int i = 0; i < CIRCLE_NUMBER_OF_POINTS; i++) {
			double tc = Math.toRadians(a);
			double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d) + Math.cos(lat1) * Math.sin(d) * Math.cos(tc));
			double lng2 = lng1
					+ Math.atan2(Math.sin(tc) * Math.sin(d) * Math.cos(lat1),
							Math.cos(d) - Math.sin(lat1) * Math.sin(lat2));
			circlePoints[i] = new org.gwtopenmaps.openlayers.client.geometry.Point(Math.toDegrees(lng2),
					Math.toDegrees(lat2));
			a += step;
		}
		LinearRing ring = new LinearRing(circlePoints);
		Style style = getStyle(circle);
		style.setLabel(text);
		return addFeature((new org.gwtopenmaps.openlayers.client.geometry.Polygon(new LinearRing[] { ring })), style);
	}

	@Override
	public PopupWindow createPopupWindow() {

		return new PopupWindow() {
			private final FlowPanel panel = new FlowPanel();
			private Popup popup;

			@Override
			public boolean remove(Widget w) {
				return panel.remove(w);
			}

			@Override
			public Iterator<Widget> iterator() {
				return panel.iterator();
			}

			@Override
			public void clear() {
				panel.clear();
			}

			@Override
			public void add(Widget w) {
				panel.add(w);
			}

			@Override
			public void open(Point location) {
                                LonLat popupPosition = OpenLayersAdapter.getLatLng(location);
                                popupPosition.transform("EPSG:4326", map.getProjection());
				popup = new Popup(location.getUri(), popupPosition, new Size(200, 100),
						DOM.getInnerHTML(panel.getElement()), true);
				popup.setBorder("1px solid #424242");
				map.addPopupExclusive(popup);
			}

			@Override
			public void close() {
				if (popup != null) {
					map.removePopup(popup);
				}
			}
		};
	}

	@Override
	public void clear() {
		for (VectorFeature feature : features) {
			vectorLayer.removeFeature(feature);
                        vectorBckgLayer.removeFeature(feature);
                        
		}
		features.clear();
	}

	@Override
	public MapView getMapView() {
		return owner;
	}

	public class FeatureHasClickHandlerWrapper implements HasClickHandlers {

		private final String featureId;

		private FeatureHasClickHandlerWrapper(String featureId) {
			this.featureId = featureId;
		}

		@Override
		public void fireEvent(GwtEvent<?> event) {
			// ignore
		}

		@Override
		public HandlerRegistration addClickHandler(final ClickHandler handler) {
			List<ClickHandler> clickHandlers = handlers.get(featureId);
			if (clickHandlers == null) {
				clickHandlers = new ArrayList<ClickHandler>();
				handlers.put(featureId, clickHandlers);
			}
			final List<ClickHandler> fClickHandlers = clickHandlers;
			clickHandlers.add(handler);
			return new HandlerRegistration() {

				@Override
				public void removeHandler() {
					fClickHandlers.remove(handler);
				}
			};
		}
	}

	@Override
	public void onFeatureSelected(FeatureSelectedEvent eventObject) {
		List<ClickHandler> clickHandlers = handlers.get(eventObject.getVectorFeature().getAttributes()
				.getAttributeAsString("map4rdf_id"));
		if (clickHandlers != null) {
			for (ClickHandler handler : clickHandlers) {
				handler.onClick(null);
			}
		}
	}

	void bind() {
		vectorLayer.addVectorFeatureSelectedListener(this);
		SelectFeature selectFeature = new SelectFeature(vectorLayer);
		map.addControl(selectFeature);
		selectFeature.activate();
	}

	/* ------------------------- helper methods -- */
	private org.gwtopenmaps.openlayers.client.geometry.Point[] getPoints(List<Point> points) {
		org.gwtopenmaps.openlayers.client.geometry.Point[] pointsArray = new org.gwtopenmaps.openlayers.client.geometry.Point[points
				.size()];
		int index = 0;
		for (Point p : points) {
			pointsArray[index++] = new org.gwtopenmaps.openlayers.client.geometry.Point(p.getX(), p.getY());
		}
		return pointsArray;
	}

	private HasClickHandlers addFeature(Geometry geometry, Style style) {
		VectorFeature feature = new VectorFeature(geometry);
		if (style != null) {
			feature.setStyle(style);
		}
		String featureId = DOM.createUniqueId();
		feature.getAttributes().setAttribute("map4rdf_id", featureId);
		vectorLayer.addFeature(feature);
		features.add(feature);

		return new FeatureHasClickHandlerWrapper(featureId);
	}
        private HasClickHandlers addFlatFeature(Geometry geometry, Style style) {
		VectorFeature feature = new VectorFeature(geometry);
		if (style != null) {
			feature.setStyle(style);
		}
		String featureId = DOM.createUniqueId();
		feature.getAttributes().setAttribute("map4rdf_id", featureId);
		vectorBckgLayer.addFeature(feature);
                features.add(feature);
		return new FeatureHasClickHandlerWrapper(featureId);
	}
        
        

	private Style getStyle(StyleMapShape<?> styleMapShape) {
		Style style = new Style();
		style.setFillColor(styleMapShape.getFillColor());
		style.setFillOpacity(styleMapShape.getFillOpacity());
		style.setStrokeColor(styleMapShape.getStrokeColor());
		style.setStrokeOpacity(styleMapShape.getStrokeOpacity());
		style.setStrokeWidth(styleMapShape.getStrokeWidth());
		style.setCursor("pointer");
		return style;

	}

	private Style getStyle(org.gwtopenmaps.openlayers.client.geometry.Point olPoint) {
		Style style = new Style();
		style.setExternalGraphic(GWT.getModuleBaseURL() + MARKER_ICON);
		style.setGraphicSize(24, 21);
		style.setFillOpacity(1);
		style.setCursor("pointer");
		return style;
	}

	private Style getTextStyle(String text) {
		Style style = new Style();
		style.setLabel(text);
		style.setCursor("pointer");
		style.setPointRadius(20);
		return style;
	}
        
        public void refreshContent(){

            if(this != null && this.map!=null) {
                //setCenter!!
               // this.owner.setMapProjectionString(mapProjecetion);
                //result += this.owner.getMapProjectionString()+ this.map.getProjection();

                LonLat ll3 = new LonLat(-3.703637, 40.416645);
                ll3.transform("EPSG:EPSG:4326", this.map.getProjection());
                map.setCenter(ll3,4);
                //if there is a flat map
                if(this.vectorBckgLayer != null && this.vectorLayer !=null){
                    if(map.getProjection().equals("EPSG:900913")){
                        this.vectorBckgLayer.setIsVisible(false);
                        this.vectorLayer.setIsVisible(true);
                    }
                    //if there is a spherical map
                    else {
                        this.vectorBckgLayer.setIsVisible(true);
                        this.vectorLayer.setIsVisible(false);     
                    }               
                }
                
            }

        }
}
