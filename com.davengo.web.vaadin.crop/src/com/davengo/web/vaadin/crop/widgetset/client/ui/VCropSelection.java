package com.davengo.web.vaadin.crop.widgetset.client.ui;

import java.util.Arrays;

/**
 * The value class used to store and transform the selections in the CropImage
 * as well as the CropField. The values of the coordinates refer to the true
 * image size coordinate system (top left is x=0, y=0).
 * 
 * @author Eric Seckler
 */
public class VCropSelection {
	public enum Edge {
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
	}

	public enum Length {
		WIDTH, HEIGHT, ANY
	}

	private int xTopLeft;
	private int yTopLeft;
	private int xBottomRight;
	private int yBottomRight;

	/**
	 * Create a new empty {@link VCropSelection}.
	 */
	public VCropSelection() {
	}

	/**
	 * Create a new {@link VCropSelection} with the given coordinates.
	 * 
	 * @param xTopLeft
	 *            the horizontal coordinate of the top left corner
	 * @param yTopLeft
	 *            the vertical coordinate of the top left corner
	 * @param xBottomRight
	 *            the horizontal coordinate of the bottom right corner
	 * @param yBottomRight
	 *            the vertical coordinate of the bottom right corner
	 */
	public VCropSelection(int xTopLeft, int yTopLeft, int xBottomRight,
			int yBottomRight) {
		this.xTopLeft = xTopLeft;
		this.yTopLeft = yTopLeft;
		this.xBottomRight = xBottomRight;
		this.yBottomRight = yBottomRight;
	}

	/**
	 * Create a new {@link VCropSelection} with the coordinates given as a
	 * string array (used for extraction from Vaadin string variable form).
	 * 
	 * @param newValue
	 *            the string array containing the coordinates (same order as in
	 *            {@link VCropSelection#VCropSelection(int, int, int, int)})
	 */
	public VCropSelection(String[] newValue) {
		this(Integer.parseInt(newValue[0]), Integer.parseInt(newValue[1]),
				Integer.parseInt(newValue[2]), Integer.parseInt(newValue[3]));
	}

	/**
	 * Create a new {@link VCropSelection} with the same coordinates as in the
	 * given selection (create a copy).
	 * 
	 * @param selection
	 *            the selection to copy the coordinates from
	 */
	public VCropSelection(VCropSelection selection) {
		this.xTopLeft = selection.getXTopLeft();
		this.yTopLeft = selection.getYTopLeft();
		this.xBottomRight = selection.getXBottomRight();
		this.yBottomRight = selection.getYBottomRight();
	}

	/**
	 * @return the horizontal coordinate of the top left corner
	 */
	public int getXTopLeft() {
		return xTopLeft;
	}

	/**
	 * Set the horizontal coordinate of the top left corner
	 * 
	 * @param xTopLeft
	 *            the coordinate value
	 */
	public void setXTopLeft(int xTopLeft) {
		this.xTopLeft = xTopLeft;
	}

	/**
	 * @return the vertical coordinate of the top left corner
	 */
	public int getYTopLeft() {
		return yTopLeft;
	}

	/**
	 * Set the vertical coordinate of the top left corner
	 * 
	 * @param yTopLeft
	 *            the coordinate value
	 */
	public void setYTopLeft(int yTopLeft) {
		this.yTopLeft = yTopLeft;
	}

	/**
	 * set the coordinates of the top left corner
	 * 
	 * @param x
	 *            the horizontal coordinate value
	 * @param y
	 *            the vertical coordinate value
	 */
	public void setTopLeft(int x, int y) {
		setXTopLeft(x);
		setYTopLeft(y);
	}

	/**
	 * @return the horizontal coordinate of the bottom right corner
	 */
	public int getXBottomRight() {
		return xBottomRight;
	}

	/**
	 * Set the horizontal coordinate of the bottom right corner
	 * 
	 * @param xBottomRight
	 *            the coordinate value
	 */
	public void setXBottomRight(int xBottomRight) {
		this.xBottomRight = xBottomRight;
	}

	/**
	 * @return the vertical coordinate of the bottom right corner
	 */
	public int getYBottomRight() {
		return yBottomRight;
	}

	/**
	 * Set the vertical coordinate of the bottom right corner
	 * 
	 * @param yBottomRight
	 *            the coordinate value
	 */
	public void setYBottomRight(int yBottomRight) {
		this.yBottomRight = yBottomRight;
	}

	/**
	 * set the coordinates of the bottom right corner
	 * 
	 * @param x
	 *            the horizontal coordinate value
	 * @param y
	 *            the vertical coordinate value
	 */
	public void setBottomRight(int x, int y) {
		setXBottomRight(x);
		setYBottomRight(y);
	}

	/**
	 * Convert the selection to a string array. The return value corresponds to
	 * the format valid for {@link VCropSelection#VCropSelection(String[])}.
	 * 
	 * @return the string array representation of the selection coordinates
	 */
	public String[] toStringArray() {
		return new String[] { Integer.toString(xTopLeft),
				Integer.toString(yTopLeft), Integer.toString(xBottomRight),
				Integer.toString(yBottomRight) };
	}

	/**
	 * This {@link VCropSelection} is equal to another object if that object is
	 * a {@link VCropSelection} with the same coordinates values.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VCropSelection) {
			VCropSelection selection = (VCropSelection) obj;
			return getXTopLeft() == selection.getXTopLeft()
					&& getYTopLeft() == selection.getYTopLeft()
					&& getXBottomRight() == selection.getXBottomRight()
					&& getYBottomRight() == selection.getYBottomRight();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new int[] { getXTopLeft(), getYTopLeft(),
				getXBottomRight(), getYBottomRight() });
	}

	/**
	 * @return true if the area occupied by this {@link VCropSelection} is 0.
	 */
	public boolean isEmpty() {
		return getHeight() == 0 || getWidth() == 0;
	}

	/**
	 * @return the width of the selection (can be negative)
	 */
	public int getWidth() {
		return xBottomRight - xTopLeft;
	}

	/**
	 * @return the height of the selection (can be negative)
	 */
	public int getHeight() {
		return yBottomRight - yTopLeft;
	}

	/**
	 * @return the absolute width of the selection (always positive)
	 */
	public int getAbsoluteWidth() {
		return Math.abs(getWidth());
	}

	/**
	 * @return the absolute height of the selection (always positive)
	 */
	public int getAbsoluteHeight() {
		return Math.abs(getHeight());
	}

	/**
	 * Fix the orientation of the selection. (Fixes cases where the bottom right
	 * coordinate values are left or above the top left coordinate values.)
	 * 
	 */
	public void fixOrientation() {
		if (xBottomRight < xTopLeft) {
			int tmp = xTopLeft;
			xTopLeft = xBottomRight;
			xBottomRight = tmp;
		}

		if (yBottomRight < yTopLeft) {
			int tmp = yTopLeft;
			yTopLeft = yBottomRight;
			yBottomRight = tmp;
		}
	}

	/**
	 * Fix the position of the selection according to the given image width and
	 * height. (Moves the selection back into the image if its extent exceeds
	 * out of the image coordinate system.)
	 * 
	 * @param imageWidth
	 *            the (true) width of the image this selection is placed in
	 * @param imageHeight
	 *            the (true) height of the image this selection is placed in
	 */
	public void fixPosition(int imageWidth, int imageHeight) {
		int diffX = 0;
		int diffY = 0;

		if (getXTopLeft() < 0) {
			diffX = -getXTopLeft();
		} else if (getXBottomRight() > imageWidth) {
			diffX = imageWidth - getXBottomRight();
		}

		if (getYTopLeft() < 0) {
			diffY = -getYTopLeft();
		} else if (getYBottomRight() > imageHeight) {
			diffY = imageHeight - getYBottomRight();
		}

		move(diffX, diffY);

		if (getXTopLeft() < 0) {
			setXTopLeft(0);
		}
		if (getXBottomRight() > imageWidth) {
			setXBottomRight(imageWidth);
		}

		if (getYTopLeft() < 0) {
			setYTopLeft(0);
		}
		if (getYBottomRight() > imageHeight) {
			setYBottomRight(imageHeight);
		}
	}

	/**
	 * Move the selection horizontally and/or vertically.
	 * 
	 * @param diffX
	 *            the amount of pixels to be added to the horizontal coordinate
	 *            values
	 * @param diffY
	 *            the amount of pixels to be added to the vertical coordinate
	 *            values
	 */
	public void move(int diffX, int diffY) {
		xTopLeft += diffX;
		xBottomRight += diffX;
		yTopLeft += diffY;
		yBottomRight += diffY;
	}

	/**
	 * Enforce the extents of the selection according to the given parameters.
	 * The selection will be scaled as necessary.
	 * 
	 * @param minWidth
	 *            the minimum width of the selection
	 * @param minHeight
	 *            the minimum height of the selection
	 * @param maxWidth
	 *            the maximum width of the selection
	 * @param maxHeight
	 *            the maximum height of the selection
	 * @param imageWidth
	 *            the (real) width of the image
	 * @param imageHeight
	 *            the (real) height of the image
	 * @param aspectRatio
	 *            the aspect ratio of the selection
	 * @param aspectDecisiveLength
	 *            the length that will not be changed for aspect ratio
	 *            correction (if {@link Length#ANY}, the larger length will be
	 *            the decisive one)
	 * @param fixedEdge
	 *            the edge that will not be moved for height / width correction
	 */
	public void enforceExtents(int minWidth, int minHeight, int maxWidth,
			int maxHeight, int imageWidth, int imageHeight, float aspectRatio,
			Length aspectDecisiveLength, Edge fixedEdge) {
		int height = getHeight();
		int width = getWidth();

		if (maxHeight == 0)
			maxHeight = Integer.MAX_VALUE;
		if (maxWidth == 0)
			maxWidth = Integer.MAX_VALUE;

		switch (fixedEdge) {
		case TOP_LEFT:
		case TOP_RIGHT:
			if (height > 0) {
				maxHeight = Math.min(maxHeight, imageHeight - getYTopLeft());
			} else {
				maxHeight = Math.min(maxHeight, getYTopLeft());
			}
			break;
		case BOTTOM_LEFT:
		case BOTTOM_RIGHT:
			if (height > 0) {
				maxHeight = Math.min(maxHeight, getYBottomRight());
			} else {
				maxHeight = Math
						.min(maxHeight, imageHeight - getYBottomRight());
			}
			break;
		}

		switch (fixedEdge) {
		case TOP_LEFT:
		case BOTTOM_LEFT:
			if (width > 0) {
				maxWidth = Math.min(maxWidth, imageWidth - getXTopLeft());
			} else {
				maxWidth = Math.min(maxWidth, getXTopLeft());
			}
			break;
		case TOP_RIGHT:
		case BOTTOM_RIGHT:
			if (width > 0) {
				maxWidth = Math.min(maxWidth, getXBottomRight());
			} else {
				maxWidth = Math.min(maxWidth, imageWidth - getXBottomRight());
			}
			break;
		}

		if (aspectRatio == 0) {
			aspectDecisiveLength = Length.ANY;
		} else if (aspectRatio < 0) {
			aspectRatio = Math.abs(aspectRatio);
		}

		switch (aspectDecisiveLength) {
		case HEIGHT:
			if (Math.abs(height) < minHeight) {
				height = (int) Math.copySign(minHeight, height);
			} else if (Math.abs(height) > maxHeight) {
				height = (int) Math.copySign(maxHeight, height);
			}

			width = (int) Math
					.copySign(Math.round(aspectRatio * height), width);

			if (Math.abs(width) < minWidth) {
				width = (int) Math.copySign(minWidth, width);
				height = (int) Math.copySign(Math.round(width / aspectRatio),
						height);
			} else if (Math.abs(width) > maxWidth) {
				width = (int) Math.copySign(maxWidth, width);
				height = (int) Math.copySign(Math.round(width / aspectRatio),
						height);
			}
			break;
		case WIDTH:
			if (Math.abs(width) < minWidth) {
				width = (int) Math.copySign(minWidth, width);
			} else if (Math.abs(width) > maxWidth) {
				width = (int) Math.copySign(maxWidth, width);
			}

			height = (int) Math.copySign(Math.round(width / aspectRatio),
					height);

			if (Math.abs(height) < minHeight) {
				height = (int) Math.copySign(minHeight, height);
				width = (int) Math.copySign(Math.round(aspectRatio * height),
						width);
			} else if (Math.abs(height) > maxHeight) {
				height = (int) Math.copySign(maxHeight, height);
				width = (int) Math.copySign(Math.round(aspectRatio * height),
						width);
			}
			break;
		case ANY:
			if (Math.abs(width) < minWidth) {
				width = (int) Math.copySign(minWidth, width);
			} else if (Math.abs(width) > maxWidth) {
				width = (int) Math.copySign(maxWidth, width);
			}
			if (Math.abs(height) < minHeight) {
				height = (int) Math.copySign(minHeight, height);
			} else if (Math.abs(height) > maxHeight) {
				height = (int) Math.copySign(maxHeight, height);
			}

			if (aspectRatio != 0) {
				if (Math.abs(width / (float) height) > aspectRatio) {
					height = (int) Math.copySign(
							Math.round(width / aspectRatio), height);
				} else {
					width = (int) Math.copySign(
							Math.round(aspectRatio * height), width);
				}

				if (Math.abs(width) > minWidth && Math.abs(width) > maxWidth) {
					width = (int) Math.copySign(maxWidth, width);
					height = (int) Math.copySign(
							Math.round(width / aspectRatio), height);
				}
				if (Math.abs(height) > minHeight
						&& Math.abs(height) > maxHeight) {
					height = (int) Math.copySign(maxHeight, height);
					width = (int) Math.copySign(
							Math.round(aspectRatio * height), width);
				}
			}
			break;
		}

		switch (fixedEdge) {
		case TOP_LEFT:
			setBottomRight(getXTopLeft() + width, getYTopLeft() + height);
			break;
		case TOP_RIGHT:
			setXTopLeft(getXBottomRight() - width);
			setYBottomRight(getYTopLeft() + height);
			break;
		case BOTTOM_LEFT:
			setXBottomRight(getXTopLeft() + width);
			setYTopLeft(getYBottomRight() - height);
			break;
		case BOTTOM_RIGHT:
			setTopLeft(getXBottomRight() - width, getYBottomRight() - height);
			break;
		}
	}
}
