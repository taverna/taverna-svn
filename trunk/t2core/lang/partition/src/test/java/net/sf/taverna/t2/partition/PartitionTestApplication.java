package net.sf.taverna.t2.partition;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.TreeCellRenderer;

import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;
import net.sf.taverna.t2.partition.ui.TableTreeNodeColumn;
import net.sf.taverna.t2.partition.ui.TableTreeNodeRenderer;

/**
 * Exercise the partition algorithm codes
 * 
 * @author Tom Oinn
 * 
 */
public class PartitionTestApplication {

	public static void main(String[] args) throws InterruptedException {
		 try {
			    // Set System L&F
		        UIManager.setLookAndFeel(
		            UIManager.getSystemLookAndFeelClassName());
		    } 
		    catch (UnsupportedLookAndFeelException e) {
		       // handle exception
		    }
		    catch (ClassNotFoundException e) {
		       // handle exception
		    }
		    catch (InstantiationException e) {
		       // handle exception
		    }
		    catch (IllegalAccessException e) {
		       // handle exception
		    }

		JFrame frame = new JFrame("Partition test");
		final PropertyExtractorRegistry reg = new ExampleExtractorRegistry();
		RootPartition<ExampleItem> partition = new RootPartition<ExampleItem>(
				getAlgorithms(), reg);
		JTree partitionTree = new JTree(partition);
		partitionTree.setRowHeight(24);
		TreeCellRenderer oldRenderer = partitionTree.getCellRenderer();
		partitionTree
				.setCellRenderer(new TableTreeNodeRenderer(oldRenderer, 80) {

					@Override
					public TableTreeNodeColumn[] getColumns() {
						return new TableTreeNodeColumn[] {
								new TableTreeNodeColumn() {

									public Component getCellRenderer(
											Object value) {
										Object propertyValue = reg
												.getAllPropertiesFor(value)
												.get("float");
										if (propertyValue == null) {
											propertyValue = "Not defined";
										}
										return new JLabel(propertyValue
												.toString());
									}

									public int getColumnWidth() {
										return 100;
									}

									public String getDescription() {
										// TODO Auto-generated method stub
										return "Description....";
									}

									public String getShortName() {
										return "float";
									}

									public Color getColour() {
										return new Color(150, 150, 210);
									}

								}, new TableTreeNodeColumn() {

									public Component getCellRenderer(
											Object value) {
										Object propertyValue = reg
												.getAllPropertiesFor(value)
												.get("int");
										if (propertyValue == null) {
											propertyValue = "Not defined";
										}
										return new JLabel(propertyValue
												.toString());
									}

									public int getColumnWidth() {
										// TODO Auto-generated method stub
										return 100;
									}

									public String getDescription() {
										// TODO Auto-generated method stub
										return "blah blah";
									}

									public String getShortName() {
										// TODO Auto-generated method stub
										return "int";
									}

									public Color getColour() {
										return new Color(150, 210, 150);
									}

								}

						};

					}
				});

		frame.getContentPane().add(new JScrollPane(partitionTree));
		frame.setSize(400,200);
		frame.setVisible(true);
		for (ExampleItem item : exampleItems) {
			Thread.sleep(1000);
			partition.addOrUpdateItem(item);
		}

	}

	static ExampleItem[] exampleItems = new ExampleItem[] {
			new ExampleItem("foo", 1, 2.0f), new ExampleItem("bar", 1, 2.0f),
			new ExampleItem("foo", 4, 3.7f), new ExampleItem("foo", 3, 2.0f),
			new ExampleItem("bar", 1, 3.5f), new ExampleItem("bar", 1, 7.5f) };

	static List<PartitionAlgorithmSPI<?>> getAlgorithms() {
		List<PartitionAlgorithmSPI<?>> paList = new ArrayList<PartitionAlgorithmSPI<?>>();
		LiteralValuePartitionAlgorithm lvpa = new LiteralValuePartitionAlgorithm();
		lvpa.setPropertyName("name");
		paList.add(lvpa);
		LiteralValuePartitionAlgorithm lvpa2 = new LiteralValuePartitionAlgorithm();
		lvpa2.setPropertyName("int");
		paList.add(lvpa2);
		return paList;
	}

	static class ExampleItem {
		private String name;
		private int intValue;
		private float floatValue;

		public String getName() {
			return this.name;
		}

		public int getIntValue() {
			return this.intValue;
		}

		public float getFloatValue() {
			return this.floatValue;
		}

		public ExampleItem(String name, int intValue, float floatValue) {
			this.name = name;
			this.intValue = intValue;
			this.floatValue = floatValue;
		}

		public String toString() {
			return this.name;
		}
	}

	static class ExampleExtractorRegistry implements PropertyExtractorRegistry {
		public Map<String, Object> getAllPropertiesFor(Object target) {
			Map<String, Object> properties = new HashMap<String, Object>();
			if (target instanceof ExampleItem) {
				ExampleItem item = (ExampleItem) target;
				properties.put("name", item.getName());
				properties.put("int", item.getIntValue());
				properties.put("float", item.getFloatValue());
			}
			return properties;
		}
	}

}
