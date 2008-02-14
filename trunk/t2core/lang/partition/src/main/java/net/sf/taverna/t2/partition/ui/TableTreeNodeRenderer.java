package net.sf.taverna.t2.partition.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public abstract class TableTreeNodeRenderer implements TreeCellRenderer {

	private static final long serialVersionUID = -7291631337751330696L;

	// The difference in indentation between a node and its child nodes, there
	// isn't an easy way to get this other than constructing a JTree and
	// measuring it - you'd think it would be a property of TreeUI but
	// apparently not. 20 is a blatant guess for testing purposes!
	private static int perNodeOffset = -1;

	// Use this to rubber stamp the original node renderer in before rendering
	// the table
	private TreeCellRenderer nodeRenderer;

	// Determines the space allocated to leaf nodes and their parents when
	// applying the stamp defined by the nodeRenderer
	private int nodeWidth;

	// Number of pixels of space to leave between the node label and the table
	// header or rows
	private int labelToTablePad = 3;

	// Number of pixels to leave around the label rendered into the table cells
	private int cellPadding = 4;

	public Component getTreeCellRendererComponent(JTree tree,
			final Object value, boolean selected, boolean expanded,
			boolean leaf, final int row, boolean hasFocus) {
		final Component nodeLabel = nodeRenderer.getTreeCellRendererComponent(
				tree, value, selected, expanded, leaf, row, hasFocus);
		final int nodeLabelHeight = (int) nodeLabel.getPreferredSize()
				.getHeight();
		if (leaf) {
			// Rendering the leaf nodes, therefore use the table rendering
			// strategy
			getPerNodeIndentation(tree, row);
			return new JComponent() {
				private static final long serialVersionUID = 4993815558563895266L;

				@Override
				public Dimension getPreferredSize() {
					int width = nodeWidth + labelToTablePad;
					for (TableTreeNodeColumn column : getColumns()) {
						width += column.getColumnWidth();
					}
					return new Dimension(width, nodeLabelHeight);
				}

				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2d = (Graphics2D) g.create();
					// Enable anti-aliasing for the curved lines
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);

					GeneralPath path = new GeneralPath();
					path.moveTo(0, 0);
					path.lineTo(0, getHeight() - 1);
					path.lineTo(nodeWidth + labelToTablePad, getHeight() - 1);
					path.lineTo(nodeWidth + labelToTablePad, 0);
					path.lineTo(0, 0);
					g2d.setPaint(Color.white);
					g2d.fill(path);

					nodeLabel
							.setSize(new Dimension(nodeWidth, nodeLabelHeight));
					nodeLabel.setBackground(new Color(200, 200, 200));
					nodeLabel.paint(g2d);

					path = new GeneralPath();
					path.moveTo(1, 0);
					path.lineTo(1, getHeight() - 2);
					path.lineTo(nodeWidth + labelToTablePad - 1,
							getHeight() - 2);
					g2d.setPaint(new Color(210, 210, 210));
					g2d.draw(path);

					path = new GeneralPath();
					path.moveTo(0, 0);
					path.lineTo(0, getHeight() - 1);
					path.lineTo(nodeWidth + labelToTablePad, getHeight() - 1);
					g2d.setPaint(Color.black);
					g2d.draw(path);

					g2d.translate(nodeWidth + labelToTablePad, 0);
					boolean first = true;
					for (TableTreeNodeColumn column : getColumns()) {

						g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
								BasicStroke.JOIN_MITER));

						path = new GeneralPath();
						path.moveTo(0, 0);
						path.lineTo(0, getHeight() - 1);
						path.lineTo(column.getColumnWidth() - 1,
								getHeight() - 1);
						path.lineTo(column.getColumnWidth() - 1, 0);
						path.lineTo(0, 0);
						Color fillColor = column.getColour().brighter();
						if ((row & 1) == 1) {
							fillColor = new Color((fillColor.getRed() + column
									.getColour().getRed()) / 2,
									(fillColor.getGreen() + column.getColour()
											.getGreen()) / 2, (fillColor
											.getBlue() + column.getColour()
											.getBlue()) / 2);
						}
						g2d.setPaint(fillColor);
						g2d.fill(path);

						path = new GeneralPath();
						g2d.setPaint(fillColor.brighter());
						path.moveTo(0, 0);
						path.lineTo(column.getColumnWidth() - 2, 0);
						path.lineTo(column.getColumnWidth() - 2,
								getHeight() - 1);
						g2d.draw(path);

						Component cellComponent = column.getCellRenderer(value);
						cellComponent.setSize(new Dimension(column
								.getColumnWidth()
								- cellPadding * 2, getHeight()));
						g2d.translate(cellPadding, 0);
						cellComponent.paint(g2d);
						g2d.translate(-cellPadding, 0);

						path = new GeneralPath();
						if (first) {
							path.moveTo(0, 0);
							path.lineTo(0, getHeight() - 1);
						} else {
							path.moveTo(0, getHeight() - 1);
						}
						path.lineTo(column.getColumnWidth() - 1,
								getHeight() - 1);
						path.lineTo(column.getColumnWidth() - 1, 0);

						g2d.setPaint(Color.black);
						g2d.draw(path);

						first = false;

						g2d.translate(column.getColumnWidth(), 0);
					}
				}
			};
		} else {
			// If there are no child nodes, or there are child nodes but they
			// aren't leaves then we render the cell as normal. If there are
			// child nodes and the first one is a leaf (we assume this means
			// they all are!) then we render the table header after the label.
			if (!expanded) {
				return nodeLabel;
			}
			// Expanded so do the model check...
			TreeModel model = tree.getModel();
			int childCount = model.getChildCount(value);
			if (childCount == 0) {
				return nodeLabel;
			}
			Object childNode = model.getChild(value, 0);
			if (!model.isLeaf(childNode)) {
				return nodeLabel;
			}
			getPerNodeIndentation(tree, row);
			// Got to here so we need to render a table header.
			return new JComponent() {
				private static final long serialVersionUID = -4923965850510357216L;

				@Override
				public Dimension getPreferredSize() {
					int width = nodeWidth + labelToTablePad + perNodeOffset;
					for (TableTreeNodeColumn column : getColumns()) {
						width += column.getColumnWidth();
					}
					return new Dimension(width, (int) nodeLabel
							.getPreferredSize().getHeight());
				}

				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2d = (Graphics2D) g.create();
					// Enable anti-aliasing for the curved lines
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);

					// Paint original node label
					nodeLabel.setSize(new Dimension(nodeWidth + perNodeOffset,
							getHeight()));
					nodeLabel.paint(g2d);

					GeneralPath path = new GeneralPath();
					path.moveTo(perNodeOffset, getHeight() - 1);
					path.lineTo(perNodeOffset + nodeWidth + labelToTablePad,
							getHeight() - 1);
					g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
							BasicStroke.JOIN_MITER));
					g2d.setPaint(Color.black);
					g2d.draw(path);

					g2d.translate(nodeWidth + perNodeOffset + labelToTablePad,
							0);
					boolean first = true;
					for (TableTreeNodeColumn column : getColumns()) {

						// Fill background
						path = new GeneralPath();
						path.moveTo(0, 2);
						path.lineTo(0, getHeight() - 1);
						path.lineTo(column.getColumnWidth() - 1,
								getHeight() - 1);
						path.lineTo(column.getColumnWidth() - 1, 2);
						path.lineTo(0, 2);
						Color fillColor = column.getColour();
						g2d.setPaint(fillColor);
						g2d.fill(path);

						// Paint background highlight
						path = new GeneralPath();
						g2d.setPaint(fillColor.brighter());
						path.moveTo(0, 2);
						path.lineTo(column.getColumnWidth() - 2, 2);
						path.lineTo(column.getColumnWidth() - 2,
								getHeight() - 1);
						g2d.draw(path);

						// Paing header label
						JLabel columnLabel = new JLabel(column.getShortName());
						columnLabel.setSize(new Dimension(column
								.getColumnWidth()
								- cellPadding * 2, getHeight() - 2));
						g2d.translate(cellPadding, 2);
						columnLabel.paint(g2d);
						g2d.translate(-cellPadding, -2);

						// Paint border
						path = new GeneralPath();
						if (first) {
							path.moveTo(0, 1);
							path.lineTo(0, getHeight() - 1);
						} else {
							path.moveTo(0, getHeight() - 1);
						}
						path.lineTo(column.getColumnWidth() - 1,
								getHeight() - 1);
						path.lineTo(column.getColumnWidth() - 1, 1);
						path.lineTo(0, 1);
						first = false;
						g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
								BasicStroke.JOIN_MITER));
						g2d.draw(path);
						g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
								BasicStroke.JOIN_MITER));
						path = new GeneralPath();
						path.moveTo(0, getHeight() - 1);
						path.lineTo(column.getColumnWidth(), getHeight() - 1);
						g2d.draw(path);
						g2d.translate(column.getColumnWidth(), 0);

					}
				}
			};

		}

	}

	public abstract TableTreeNodeColumn[] getColumns();

	public TableTreeNodeRenderer(TreeCellRenderer nodeRenderer, int nodeWidth) {
		super();
		this.nodeRenderer = nodeRenderer;
		this.nodeWidth = nodeWidth;
	}
	
	private static int getPerNodeIndentation(JTree tree, int treeRow) {
		if (perNodeOffset > 0) {
			return perNodeOffset;
		}
		TreeUI uiModel = tree.getUI();
		TreePath path = tree.getPathForRow(treeRow);
		Rectangle nodeBounds = uiModel.getPathBounds(tree, path);
		Rectangle parentNodeBounds = uiModel.getPathBounds(tree, path.getParentPath());
		perNodeOffset = (int)nodeBounds.getMinX() - (int)parentNodeBounds.getMinX();
		return perNodeOffset;
	}

}
