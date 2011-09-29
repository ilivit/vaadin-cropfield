package com.davengo.web.vaadin.crop;

import java.util.Map;

import com.davengo.web.vaadin.crop.widgetset.client.ui.VCropField;
import com.davengo.web.vaadin.crop.widgetset.client.ui.VCropSelection;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ClientWidget;

/**
 * Vaadin component that shows an image with an overlayed cropping
 * functionality. The current selection can be set and get via
 * setValue()/getValue() of {@link AbstractField}. The type of that value has to
 * be {@link VCropSelection}. Usually, the selection will not be changed to a
 * <i>null</i> value. Instead, it will be an empty selection (check with
 * {@link VCropSelection#isEmpty()}).
 * 
 * @author Eric Seckler
 */
@ClientWidget(VCropField.class)
public class CropField extends AbstractField {

	private static final long serialVersionUID = -2206503821175058316L;

	protected Resource imageResource;

	protected int trueImageWidth;
	protected int trueImageHeight;
	protected int minSelectionWidth;
	protected int minSelectionHeight;
	protected int maxSelectionWidth;
	protected int maxSelectionHeight;
	protected float selectionAspectRatio;
	protected VCropSelection animateToSelection;
	protected Integer animateToDuration;

	/**
	 * Create a new CropField with a given resource as image to be cropped.
	 * 
	 * @param imageResource
	 *            the Resource to be used as image
	 */
	public CropField(Resource imageResource) {
		super();
		setImageResource(imageResource);
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		target.addAttribute(VCropField.ATTR_IMAGE_RESOURCE, imageResource);

		if (trueImageWidth != 0)
			target.addAttribute(VCropField.ATTR_TRUE_IMAGE_WIDTH,
					trueImageWidth);
		if (trueImageHeight != 0)
			target.addAttribute(VCropField.ATTR_TRUE_IMAGE_HEIGHT,
					trueImageHeight);
		if (minSelectionWidth != 0)
			target.addAttribute(VCropField.ATTR_MIN_SELECTION_WIDTH,
					minSelectionWidth);
		if (minSelectionHeight != 0)
			target.addAttribute(VCropField.ATTR_MIN_SELECTION_HEIGHT,
					minSelectionHeight);
		if (maxSelectionWidth != 0)
			target.addAttribute(VCropField.ATTR_MAX_SELECTION_WIDTH,
					maxSelectionWidth);
		if (maxSelectionHeight != 0)
			target.addAttribute(VCropField.ATTR_MAX_SELECTION_HEIGHT,
					maxSelectionHeight);
		if (selectionAspectRatio != 0)
			target.addAttribute(VCropField.ATTR_SELECTION_ASPECT_RATIO,
					selectionAspectRatio);
		if (animateToSelection != null) {
			target.addAttribute(VCropField.ATTR_ANIMATE_TO_SELECTION,
					animateToSelection.toStringArray());
			if (animateToDuration != null) {
				target.addAttribute(VCropField.ATTR_ANIMATE_TO_DURATION,
						animateToDuration);
			}
			animateToSelection = null;
			animateToDuration = null;
		}

		VCropSelection value = (VCropSelection) getValue();
		if (value == null) {
			value = new VCropSelection();
		}
		target.addVariable(this, VCropField.VAR_SELECTION,
				value.toStringArray());
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		if (variables.containsKey(VCropField.VAR_SELECTION) && !isReadOnly()) {
			Object newValue = variables.get(VCropField.VAR_SELECTION);
			if (newValue != null) {
				newValue = new VCropSelection((String[]) newValue);
			}

			boolean wasModified = isModified();
			setValue(newValue, true);
			// If the modified status changes, repaint is needed after all.
			if (wasModified != isModified()) {
				requestRepaint();
			}
		}
	}

	/**
	 * @return the Resource that was set to be used as image
	 */
	public Resource getImageResource() {
		return imageResource;
	}

	/**
	 * Change the Resource to be used as image.
	 * 
	 * @param imageResource
	 *            the Resource (a <i>null</i> value is NOT allowed)
	 */
	public void setImageResource(Resource imageResource) {
		if (imageResource == null) {
			throw new IllegalArgumentException(
					"a null value for the image resource is not allowed");
		}
		this.imageResource = imageResource;
	}

	@Override
	public Class<?> getType() {
		return VCropSelection.class;
	}

	/**
	 * @return the width that should be used for calculation of the selection
	 */
	public int getTrueImageWidth() {
		return trueImageWidth;
	}

	/**
	 * <p>
	 * If you choose to present the image in a scaled mode (i.e. by setting the
	 * extent of this component), you can set the true width of the image
	 * (non-scaled width), so that the selection the user performs on the scaled
	 * image is rescaled to fit the true image width.
	 * </p>
	 * <p>
	 * All other selection coordinates/extents will always correlate to the true
	 * image coordinate system.
	 * </p>
	 * <p>
	 * If no true image width is set (i.e. set to 0), the width of the (scaled)
	 * component is used to form the true image height.
	 * </p>
	 * 
	 * @param trueImageWidth
	 *            the width that should be used for calculation of the selection
	 */
	public void setTrueImageWidth(int trueImageWidth) {
		this.trueImageWidth = trueImageWidth;
		requestRepaint();
	}

	/**
	 * @return the height that should be used for calculation of the selection
	 */
	public int getTrueImageHeight() {
		return trueImageHeight;
	}

	/**
	 * <p>
	 * If you choose to present the image in a scaled mode (i.e. by setting the
	 * extent of this component), you can set the true height of the image
	 * (non-scaled height), so that the selection the user performs on the
	 * scaled image is rescaled to fit the true image height.
	 * </p>
	 * <p>
	 * All other selection coordinates/extents will always correlate to the true
	 * image coordinate system.
	 * </p>
	 * <p>
	 * If no true image height is set (i.e. set to 0), the height of the
	 * (scaled) component is used to form the true image height.
	 * </p>
	 * 
	 * @param trueImageHeight
	 *            the height that should be used for calculation of the selection
	 */
	public void setTrueImageHeight(int trueImageHeight) {
		this.trueImageHeight = trueImageHeight;
		requestRepaint();
	}

	/**
	 * @return the minimum width of a non-empty selection
	 */
	public int getMinSelectionWidth() {
		return minSelectionWidth;
	}

	/**
	 * Set the minimum width of a non-empty selection. The client side component
	 * will force the selection extent to correspond to this value.
	 * 
	 * @param minSelectionWidth
	 *            the minimum width of a non-empty selection
	 */
	public void setMinSelectionWidth(int minSelectionWidth) {
		this.minSelectionWidth = minSelectionWidth;
		requestRepaint();
	}

	/**
	 * @return the minimum height of a non-empty selection
	 */
	public int getMinSelectionHeight() {
		return minSelectionHeight;
	}

	/**
	 * Set the minimum height of a non-empty selection. The client side
	 * component will force the selection extent to correspond to this value.
	 * 
	 * @param minSelectionHeight
	 *            the minimum height of a non-empty selection
	 */
	public void setMinSelectionHeight(int minSelectionHeight) {
		this.minSelectionHeight = minSelectionHeight;
		requestRepaint();
	}

	/**
	 * @return the maximum width of a selection
	 */
	public int getMaxSelectionWidth() {
		return maxSelectionWidth;
	}

	/**
	 * Set the maximum width of a selection. The client side component will
	 * force the selection extent to correspond to this value.
	 * 
	 * @param maxSelectionWidth
	 *            the maximum width of a selection
	 */
	public void setMaxSelectionWidth(int maxSelectionWidth) {
		this.maxSelectionWidth = maxSelectionWidth;
		requestRepaint();
	}

	/**
	 * @return the maximum height of a selection
	 */
	public int getMaxSelectionHeight() {
		return maxSelectionHeight;
	}

	/**
	 * Set the maximum height of a selection. The client side component will
	 * force the selection extent to correspond to this value.
	 * 
	 * @param maxSelectionHeight
	 *            the maximum height of a selection
	 */
	public void setMaxSelectionHeight(int maxSelectionHeight) {
		this.maxSelectionHeight = maxSelectionHeight;
		requestRepaint();
	}

	/**
	 * @return the aspect ratio (width / height) of any selection
	 */
	public float getSelectionAspectRatio() {
		return selectionAspectRatio;
	}

	/**
	 * Set the aspect ratio (width / height) the selection has to conform to.
	 * Can be used in combination with min/max width/height. The client side
	 * component will force the selection extent to correspond to this value.
	 * 
	 * @param selectionAspectRatio
	 *            the aspect ratio (width / height) of any selection
	 */
	public void setSelectionAspectRatio(float selectionAspectRatio) {
		this.selectionAspectRatio = selectionAspectRatio;
		requestRepaint();
	}

	/**
	 * animate the change of the current selection to the given new selection
	 * with a default duration defined by the client side component.
	 * 
	 * @param selection
	 *            the new selection (a <i>null</i> value is NOT allowed, use an
	 *            empty selection for such a case)
	 */
	public void animateTo(VCropSelection selection) {
		animateTo(selection, null);
	}

	/**
	 * animate the change of the current selection to the given new selection
	 * with the given duration.
	 * 
	 * @param selection
	 *            the new selection (a <i>null</i> value is NOT allowed, use an
	 *            empty selection for such a case)
	 * @param duration
	 *            the duration of the selection in milliseconds or <i>null</i>
	 *            for a default duration
	 */
	public void animateTo(VCropSelection selection, Integer duration) {
		if (selection == null) {
			throw new IllegalArgumentException(
					"a null value for the selection is not allowed");
		}
		this.animateToSelection = selection;
		this.animateToDuration = duration;
		requestRepaint();
	}

}
