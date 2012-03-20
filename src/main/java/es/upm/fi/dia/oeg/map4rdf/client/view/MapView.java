/**
 * Copyright (c) 2011 Ontology Engineering Group, 
 * Departamento de Inteligencia Artificial,
 * Facultad de Inform�tica, Universidad 
 * Polit�cnica de Madrid, Spain
 * 
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
package es.upm.fi.dia.oeg.map4rdf.client.view;

import java.util.List;

import name.alexdeleon.lib.gwtblocks.client.widget.loading.LoadingWidget;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.maps.client.MapType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.Control;
import com.google.gwt.maps.client.control.ControlAnchor;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.LargeMapControl3D;
import com.google.gwt.maps.client.control.MenuMapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import es.upm.fi.dia.oeg.map4rdf.client.presenter.MapPresenter;
import es.upm.fi.dia.oeg.map4rdf.client.resource.BrowserMessages;
import es.upm.fi.dia.oeg.map4rdf.client.resource.BrowserResources;
import es.upm.fi.dia.oeg.map4rdf.client.widget.WidgetFactory;
import es.upm.fi.dia.oeg.map4rdf.client.widget.mapcontrol.GeoResourcesMapControl;
import es.upm.fi.dia.oeg.map4rdf.client.widget.mapcontrol.MapControl;
import es.upm.fi.dia.oeg.map4rdf.client.widget.mapcontrol.StatisticsMapControl;
import es.upm.fi.dia.oeg.map4rdf.share.GeoResource;

/**
 * @author Alexander De Leon
 */
public class MapView extends Composite implements MapPresenter.Display {

	/**
	 * By default the map is centered in Puerta del Sol, Madrid
	 */
	private static final LatLng DEFAULT_CENTER = LatLng.newInstance(40.416645, -3.703637);
	private static final int DEFAULT_ZOOM_LEVEL = 7;

	private MapWidget map;
	private Panel panel;
	private final LoadingWidget loadingWidget;
	private final GeoResourcesMapControl geoResourcesMapControl;
	private final Image kmlButton;
	private final BrowserMessages geoMessages;

	@Inject
	public MapView(WidgetFactory widgetFactory, StatisticsMapControl statisticsMapControl,
			GeoResourcesMapControl geoResourcesMapControl, BrowserMessages geoMessages,
			BrowserResources browserResources) {
		this.geoResourcesMapControl = geoResourcesMapControl;
		loadingWidget = widgetFactory.getLoadingWidget();
		this.geoMessages = geoMessages;
		kmlButton = createKMLButton(browserResources);

		initWidget(createUi());

		addMapControl(statisticsMapControl);
		addMapControl(geoResourcesMapControl);
	}

	@Override
	public MapWidget getMap() {
		return map;
	}

	@Override
	public void drawGeoResouces(List<GeoResource> resources) {
		geoResourcesMapControl.drawGeoResouces(resources, geoMessages);
	}

	@Override
	public void clear() {
		geoResourcesMapControl.clear();
	}

	@Override
	public HasClickHandlers getKmlButton() {
		return kmlButton;
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
		loadingWidget.waitOn(this);

	}

	@Override
	public void stopProcessing() {
		loadingWidget.release(this);

	}

	@Override
	protected void onLoad() {
		/*
		 * This is woraround for issue with the GoogleMaps and the new GWT
		 * Layout panels. More info:
		 * http://code.google.com/p/gwt-google-apis/issues/detail?id=366
		 */
		super.onLoad();
		new Timer() {

			@Override
			public void run() {
				map.checkResizeAndCenter();

			}
		}.schedule(1);
	}

	/* --------------- helper methods -- */
	protected void addMapControl(MapControl mapControl) {
		mapControl.setDisplay(this);
		map.addControl(mapControl.getCustomControl());
	}

	private Widget createUi() {
		panel = new FlowPanel();
		map = new MapWidget(DEFAULT_CENTER, DEFAULT_ZOOM_LEVEL);
		map.setSize("100%", "100%");

		map.addControl(new LargeMapControl3D());
		map.addControl(new MenuMapTypeControl());
		map.addControl(new ScaleControl());

		map.setScrollWheelZoomEnabled(false);
		map.setCurrentMapType(MapType.getPhysicalMap());

		map.addControl(new Control.CustomControl(new ControlPosition(ControlAnchor.TOP_RIGHT, 100, 8)) {

			@Override
			public boolean isSelectable() {
				return false;
			}

			@Override
			protected Widget initialize(MapWidget map) {
				return kmlButton;
			}
		});

		panel.add(map);
		return panel;
	}

	private Image createKMLButton(BrowserResources browserResources) {
		Image button = new Image(browserResources.kmlButton());
		return button;
	}
}