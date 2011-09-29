package com.davengo.web.vaadin.crop;

import com.davengo.web.vaadin.crop.widgetset.client.ui.VCropSelection;
import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * A Vaadin application demonstrating the features of the {@link CropField}
 * component.
 * 
 * @author Eric Seckler
 */
public class CropDemoApplication extends Application {
	protected static final int TRUE_WIDTH = 1000;
	protected static final int TRUE_HEIGHT = 740;
	protected static final long serialVersionUID = -1390909809187605613L;

	protected VerticalLayout mainLayout;
	protected GridLayout buttonBar;
	protected HorizontalLayout infoBar;
	protected CropField cropField;
	protected Button animateButton;
	protected Button readOnlyButton;
	protected Button enableButton;
	protected Button aspectButton;
	protected Button minSizeButton;
	protected Button maxSizeButton;
	protected Label selectionInfoLabel;
	protected HorizontalLayout selectionTextFields;
	protected TextField selectionTextFieldXTopLeft;
	protected TextField selectionTextFieldYTopLeft;
	protected TextField selectionTextFieldXBottomRight;
	protected TextField selectionTextFieldYBottomRight;

	@Override
	public void init() {
		setTheme("cropdemo");

		Window mainWindow = new Window("Crop Demo Application");
		mainLayout = new VerticalLayout();
		mainWindow.setContent(mainLayout);
		mainLayout.setSpacing(true);
		mainLayout.setMargin(true);

		Label label = new Label(
				"Below, you can see a crop widget example. Feel free to experiment with it.");
		mainLayout.addComponent(label);

		cropField = new CropField(new ClassResource("flowers.jpg", this));
		mainLayout.addComponent(cropField);
		cropField.setTrueImageWidth(TRUE_WIDTH);
		cropField.setTrueImageHeight(TRUE_HEIGHT);
		// cropField.setValue(new VCropSelection(40,60,480,320));

		buttonBar = new GridLayout(3, 2);
		mainLayout.addComponent(buttonBar);
		buttonBar.setSpacing(true);
		buttonBar.setWidth("500px");

		animateButton = new Button();
		buttonBar.addComponent(animateButton);
		animateButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -7422736104946684297L;

			@Override
			public void buttonClick(ClickEvent event) {
				animateToRandomSelection();
			}
		});
		animateButton.setCaption("Animate");
		animateButton.setWidth("100%");

		readOnlyButton = new Button();
		buttonBar.addComponent(readOnlyButton);
		readOnlyButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -7422736104946684297L;

			@Override
			public void buttonClick(ClickEvent event) {
				toggleReadOnly();
			}
		});
		readOnlyButton.setCaption("Toggle Read Only");
		readOnlyButton.setWidth("100%");

		enableButton = new Button();
		buttonBar.addComponent(enableButton);
		enableButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -7422736104946684297L;

			@Override
			public void buttonClick(ClickEvent event) {
				toggleEnablement();
			}
		});
		enableButton.setCaption("Toggle Enablement");
		enableButton.setWidth("100%");

		aspectButton = new Button();
		buttonBar.addComponent(aspectButton);
		aspectButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -7422736104946684297L;

			@Override
			public void buttonClick(ClickEvent event) {
				toggleAspect();
			}
		});
		aspectButton.setCaption("Toggle Aspect");
		aspectButton.setWidth("100%");

		minSizeButton = new Button();
		buttonBar.addComponent(minSizeButton);
		minSizeButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -7422736104946684297L;

			@Override
			public void buttonClick(ClickEvent event) {
				toggleMinSize();
			}
		});
		minSizeButton.setCaption("Toggle Min Size");
		minSizeButton.setWidth("100%");

		maxSizeButton = new Button();
		buttonBar.addComponent(maxSizeButton);
		maxSizeButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -7422736104946684297L;

			@Override
			public void buttonClick(ClickEvent event) {
				toggleMaxSize();
			}
		});
		maxSizeButton.setCaption("Toggle Max Size");
		maxSizeButton.setWidth("100%");

		infoBar = new HorizontalLayout();
		mainLayout.addComponent(infoBar);
		infoBar.setSpacing(true);

		selectionInfoLabel = new Label();
		infoBar.addComponent(selectionInfoLabel);
		selectionInfoLabel.setValue("Selection: nothing selected.");

		selectionTextFields = new HorizontalLayout();
		infoBar.addComponent(selectionTextFields);
		selectionTextFields.setSpacing(true);

		selectionTextFields.addComponent(new Label("X1:"));
		selectionTextFieldXTopLeft = new TextField();
		selectionTextFieldXTopLeft.setWidth("40px");
		selectionTextFields.addComponent(selectionTextFieldXTopLeft);
		selectionTextFields.addComponent(new Label("Y1:"));
		selectionTextFieldYTopLeft = new TextField();
		selectionTextFieldYTopLeft.setWidth("40px");
		selectionTextFields.addComponent(selectionTextFieldYTopLeft);
		selectionTextFields.addComponent(new Label("X2:"));
		selectionTextFieldXBottomRight = new TextField();
		selectionTextFieldXBottomRight.setWidth("40px");
		selectionTextFields.addComponent(selectionTextFieldXBottomRight);
		selectionTextFields.addComponent(new Label("Y2:"));
		selectionTextFieldYBottomRight = new TextField();
		selectionTextFieldYBottomRight.setWidth("40px");
		selectionTextFields.addComponent(selectionTextFieldYBottomRight);

		selectionTextFields.setVisible(false);

		cropField.setImmediate(true);
		cropField.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = -8317773834498970664L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				VCropSelection newSelection = (VCropSelection) event
						.getProperty().getValue();
				if (newSelection.isEmpty()) {
					selectionInfoLabel.setValue("Selection: nothing selected.");
					selectionTextFields.setVisible(false);
				} else {
					selectionInfoLabel.setValue("Selection: ");
					selectionTextFieldXTopLeft.setValue(Integer
							.toString(newSelection.getXTopLeft()));
					selectionTextFieldYTopLeft.setValue(Integer
							.toString(newSelection.getYTopLeft()));
					selectionTextFieldXBottomRight.setValue(Integer
							.toString(newSelection.getXBottomRight()));
					selectionTextFieldYBottomRight.setValue(Integer
							.toString(newSelection.getYBottomRight()));
					selectionTextFields.setVisible(true);
				}
			}
		});

		setMainWindow(mainWindow);
	}

	protected void animateToRandomSelection() {
		VCropSelection animateToSelection = new VCropSelection(
				(int) (Math.random() * TRUE_WIDTH),
				(int) (Math.random() * TRUE_HEIGHT),
				(int) (Math.random() * TRUE_WIDTH),
				(int) (Math.random() * TRUE_HEIGHT));
		animateToSelection.fixOrientation();
		cropField.animateTo(animateToSelection);
	}

	protected void toggleReadOnly() {
		cropField.setReadOnly(!cropField.isReadOnly());
	}

	protected void toggleEnablement() {
		cropField.setEnabled(!cropField.isEnabled());
	}

	protected void toggleAspect() {
		float newAspect = cropField.getSelectionAspectRatio() == 0 ? (16 / (float) 9)
				: 0;
		cropField.setSelectionAspectRatio(newAspect);
	}

	protected void toggleMinSize() {
		if (cropField.getMinSelectionWidth() != 0) {
			cropField.setMinSelectionWidth(0);
			cropField.setMinSelectionHeight(0);
		} else {
			cropField.setMinSelectionWidth(160);
			cropField.setMinSelectionHeight(90);
		}
	}

	protected void toggleMaxSize() {
		if (cropField.getMaxSelectionWidth() != 0) {
			cropField.setMaxSelectionWidth(0);
			cropField.setMaxSelectionHeight(0);
		} else {
			cropField.setMaxSelectionWidth(400);
			cropField.setMaxSelectionHeight(225);
		}
	}
}
