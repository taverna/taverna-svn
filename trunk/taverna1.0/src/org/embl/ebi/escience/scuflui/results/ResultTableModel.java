/*
 * Created on Aug 31, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 */
public class ResultTableModel implements TableModel
{

	private ScuflModel model;
	WorkflowInstance instance;
	HashMap provenance;
	private int rowCount;

	private ArrayList tableListeners = new ArrayList();

	private ResultTableColumn[] columns;

	/**
	 * @param model
	 * @param instance
	 */
	public ResultTableModel(ScuflModel model, WorkflowInstance instance)
	{
		this.model = model;
		this.instance = instance;
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return columns.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount()
	{
		return rowCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int columnIndex)
	{
		// TODO Implement ResultTableModel.getColumnClass
		return ResultTableCell.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		return columns[columnIndex].getCell(rowIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		// No! No set value
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int columnIndex)
	{
		return columns[columnIndex].toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void addTableModelListener(TableModelListener l)
	{
		tableListeners.add(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void removeTableModelListener(TableModelListener l)
	{
		tableListeners.remove(l);
	}

	private void init()
	{
		DataConstraint[] links = model.getDataConstraints();
		HashMap columnMap = new HashMap();
		for (int index = 0; index < links.length; index++)
		{
			DataThing thing = null;
			Processor processor = links[index].getSource().getProcessor();
			if (processor.getName().equals("SCUFL_INTERNAL_SOURCEPORTS"))
			{
				try
				{
					Map[] maps = instance.getIntermediateResultsForProcessor(links[index].getSink()
							.getProcessor().getName());
					Map outputs = maps[0];
					thing = (DataThing) outputs.get(links[index].getSink().getName());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			try
			{
				Map[] maps = instance.getIntermediateResultsForProcessor(processor.getName());
				Map outputs = maps[1];
				thing = (DataThing) outputs.get(links[index].getSource().getName());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			columnMap.put(links[index].getSource(), new ResultTableColumn(this, links[index], thing));
			//columns[index] = new ResultTableColumn(links[index]);
		}
		for (int index = 0; index < links.length; index++)
		{
			ResultTableColumn column = (ResultTableColumn) columnMap.get(links[index].getSource());
			Processor sinkProcessor = links[index].getSink().getProcessor();
			Processor sourceProcessor = links[index].getSource().getProcessor();
			for (int innerindex = 0; innerindex < links.length; innerindex++)
			{
				if (sourceProcessor.equals(links[innerindex].getSink().getProcessor()))
				{
					column.addInput((ResultTableColumn) columnMap
							.get(links[innerindex].getSource()));
				}
				if (sinkProcessor.equals(links[innerindex].getSource().getProcessor()))
				{
					column.addOutput((ResultTableColumn) columnMap.get(links[innerindex]
							.getSource()));
				}

			}
		}

		columns = new ResultTableColumn[columnMap.size()];
		columnMap.values().toArray(columns);
		Arrays.sort(columns, new Comparator()
		{
			public boolean equals(Object obj)
			{
				return false;
			}

			public int compare(Object o1, Object o2)
			{
				return ((ResultTableColumn) o1).getDepth() - ((ResultTableColumn) o2).getDepth();
			}
		});
		update();
	}

	private HashMap parseProvenance(String provenanceXML) throws JDOMException, IOException
	{
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new StringReader(provenanceXML));
		Element root = doc.getRootElement();
		System.out.println(root.getName());
		HashMap provenance = new HashMap();
		for (int index = 0; index < root.getChildren().size(); index++)
		{
			Element item = (Element) root.getChildren().get(index);
			// HACK: Hackity hackity hack. I mean... really.
			String rdf = item.getText();
			rdf = rdf.replaceAll("&lt;", "<");
			rdf = rdf.replaceAll("&amp;", "&");
			rdf = rdf.replaceAll("&gt;", ">");
			// HACK: Another hack, 'cos someone can't build valid xml...
			int tvpIndex = rdf.indexOf("tavp:createdFrom");
			if (tvpIndex != -1)
			{
				rdf = rdf.substring(0, tvpIndex + 16)
						+ " xmlns:tavp=\"http://org.embl.ebi.escience/taverna-provenance\" "
						+ rdf.substring(tvpIndex + 16);
			}
			Document itemDoc = builder.build(new StringReader(rdf));
			Element description = itemDoc.getRootElement();
			String aboutLSID = description.getAttributeValue("about", description
					.getNamespace("rdf"));
			for (int inner = 0; inner < description.getChildren().size(); inner++)
			{
				Element relationship = (Element) description.getChildren().get(inner);
				if (relationship.getName().equals("createdFrom"))
				{
					ArrayList createdList = (ArrayList) provenance.get(aboutLSID);
					if (createdList == null)
					{
						createdList = new ArrayList();
					}
					createdList.add(relationship.getAttributeValue("resource", description
							.getNamespace("rdf")));
					provenance.put(aboutLSID, createdList);
				}
			}

		}
		return provenance;
	}

	public void update()
	{
		try
		{
			provenance = parseProvenance(instance.getProvenanceXMLString());

			int row = 0;
			for (int index = columns.length - 1; index > 0; index--)
			{
				if (!columns[index].hasOutputs())
				{
					System.out.println("Getting data for " + columns[index]);
					DataThing result = columns[index].getDataThing();
					if (result != null)
					{
						row = columns[index].createCell(result, row, null).endRow + 1;
						System.out.println(result.getLSID(result.getDataObject()));
					}
				}
			}
			rowCount = row;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	void getAllInputs(DataThing thing, Collection inputLSIDs)
	{
		if (thing != null)
		{
			ArrayList moreInputs = (ArrayList) provenance.get(thing.getLSID(thing.getDataObject()));
			if (moreInputs != null)
			{
				inputLSIDs.addAll(moreInputs);
			}
		}
	}

	DataThing findThing(DataThing thing, String lsid, Collection inputLSIDs)
	{
		if (lsid.equals(thing.getLSID(thing.getDataObject())))
		{
			getAllInputs(thing, inputLSIDs);
			return thing;
		}
		if (thing.getDataObject() instanceof List)
		{
			Iterator children = thing.childIterator();
			while (children.hasNext())
			{
				DataThing child = (DataThing) children.next();
				DataThing result = findThing(child, lsid, inputLSIDs);
				if(result != null)
				{
					getAllInputs(thing, inputLSIDs);
					return result;
				}
			}
		}
		return null;
	}
}