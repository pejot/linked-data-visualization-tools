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
package es.upm.fi.dia.oeg.map4rdf.client.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.gen2.picker.client.SliderBar;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import es.upm.fi.dia.oeg.map4rdf.share.Year;

/**
 * @author Alexander De Leon
 */
public class Timeline extends SimplePanel implements HasValueChangeHandlers<Year> {

	public interface Stylesheet {
		String timeline();
	}

	private final FlowPanel panel = new FlowPanel();
	private List<Year> sortedYears;
	private double prevVal = -1;
	private boolean mouseDown;
	private SliderBar slider;

	public Timeline(Stylesheet stylesheet) {
		addStyleName(stylesheet.timeline());
	}

	public void setYears(Collection<Year> years) {
		sortedYears = sortYears(years);
		if (slider != null) {
			remove(slider);
		}
		slider = createSlider(years.size());
		setWidget(slider);
		setVisible(true);
	}

	public void hide() {
		setVisible(false);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Year> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	public void setCurrentValue(Year year, boolean fireEvent) {
		setCurrentValue(Collections.binarySearch(sortedYears, year, new YearComparator()), fireEvent);
	}

	public void setCurrentValue(int index, boolean fireEvent) {
		slider.setCurrentValue(index, fireEvent);
		if (!fireEvent) {
			prevVal = index;
		}
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		SliderBar.injectDefaultCss();
	}

	private SliderBar createSlider(int years) {
		SliderBar slider = new SliderBar(0, years - 1, new SliderBar.LabelFormatter() {
			@Override
			public String formatLabel(SliderBar slider, double value) {
				long pos = Math.round(value);
				return Integer.toString(sortedYears.get((int) pos).getValue());
			}
		});
		slider.setStepSize(1.0);
		slider.setNumTicks(years);
		slider.setNumLabels(years);
		slider.setWidth((years * 5) + "em");

		bindEvents(slider);

		return slider;
	}

	private void bindEvents(SliderBar slider) {
		slider.addMouseDownHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				mouseDown = true;
			}
		});
		slider.addMouseUpHandler(new MouseUpHandler() {

			@Override
			public void onMouseUp(MouseUpEvent event) {
				mouseDown = false;

			}
		});
		slider.addValueChangeHandler(new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
				if (mouseDown) {
					return;
				}
				double val = event.getValue();
				if (val == prevVal) {
					return;
				}
				double intVal = Math.floor(val);
				if (val - intVal == 0) {
					prevVal = val;
					ValueChangeEvent.fire(Timeline.this, sortedYears.get((int) intVal));
				}
			}
		});

	}

	private List<Year> sortYears(Collection<Year> years) {
		List<Year> list = new ArrayList<Year>(years);
		Collections.sort(list, new YearComparator());
		return list;
	}

	private static class YearComparator implements Comparator<Year> {
		@Override
		public int compare(Year o1, Year o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}
}
