# Introduction #

Below, you can find a simple instantiation example. It adds a ValueChangeListener to update a Label with the selected area coordinates.

# Details #

```
final Label selectionLabel = new Label();

final CropField cropField = new CropField(new ExternalResource("flowers.jpg"));
cropField.setImmediate(true);
cropField.addListener(new ValueChangeListener() {
                private static final long serialVersionUID = -8317773834498970664L;

                @Override
                public void valueChange(ValueChangeEvent event) {
                        VCropSelection newSelection = (VCropSelection) event
                                        .getProperty().getValue();
                        int x1 = newSelection.getXTopLeft();
                        int y1 = newSelection.getYTopLeft();
                        int x2 = newSelection.getXBottomRight();
                        int y2 = newSelection.getYBottomRight();
                        selectionLabel.setValue("Selection: x1: " + x1 + ", y1: " + y1 + ", x2: " + x2 + ", y2: " + y2);
                }
        }
});
```