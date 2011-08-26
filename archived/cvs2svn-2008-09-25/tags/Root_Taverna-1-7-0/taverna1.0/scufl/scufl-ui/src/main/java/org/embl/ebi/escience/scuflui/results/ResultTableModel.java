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
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.3 $
 */
public class ResultTableModel implements TableModel
{
	private ScuflModel model;
	WorkflowInstance instance;
	HashMap provenance;
	private int rowCount;

	private ResultSource[] sources = null;
	private boolean condenseColumns = true;

	private Collection tableListeners = new ArrayList();

	private ResultTableColumn[] columns;

	/**
	 * @param model
	 * @param instance
	 */
	public ResultTableModel(ScuflModel model, WorkflowInstance instance)
	{
		this.model = model;
		this.instance = instance;
		createColumns();
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
		return ResultTableCell.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		return columns[columnIndex].getValue(rowIndex);
	}

	public ResultTableColumn getColumn(int column)
	{
		return columns[column];
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

	/**
	 * 
	 * @param link
	 * @return a new <code>ResultSource</code> built from the given link
	 */
	private ResultSource createSource(DataConstraint link)
	{
		// Get DataThing
		DataThing thing = null;
		Processor processor = link.getSource().getProcessor();
		if (processor.getName().equals("SCUFL_INTERNAL_SOURCEPORTS"))
		{
			try
			{
				if(link.getSink().getProcessor().getName().equals("SCUFL_INTERNAL_SINKPORTS"))
				{
					thing = (DataThing)instance.getOutput().get(link.getSink().getName());
				}
				else
				{
				Map[] maps = instance.getIntermediateResultsForProcessor(link.getSink()
							.getProcessor().getName());
					Map outputs = maps[0];
					thing = (DataThing) outputs.get(link.getSink().getName());
				}
			}
			catch (Exception e)
			{
				e.printStackTrace(); 
			}
		}
		else
		{
			try
			{
				Map[] maps = instance.getIntermediateResultsForProcessor(processor.getName());
				Map outputs = maps[1];
				thing = (DataThing) outputs.get(link.getSource().getName());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		ResultSource source = new ResultSource(link.getSource(), thing);
		source.populateResults(provenance);
		return source;
	}

	/**
	 * 
	 *  
	 */
	private void createSources()
	{
		if (sources == null)
		{
			try
			{
				provenance = parseProvenance(instance.getProvenanceXMLString());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			HashMap sourceMap = new HashMap();
			DataConstraint[] links = model.getDataConstraints();
			for (int index = 0; index < links.length; index++)
			{
				ResultSource source = (ResultSource) sourceMap.get(links[index].getSource());
				if (source == null)
				{
					source = createSource(links[index]);
					sourceMap.put(links[index].getSource(), source);
				}
				source.addOutputProcessor(links[index].getSink().getProcessor());
			}

			// Build graph of sources
			Iterator sourceIterator = sourceMap.values().iterator();
			while (sourceIterator.hasNext())
			{
				ResultSource source = (ResultSource) sourceIterator.next();
				Iterator processorIterator = source.outputProcessors.iterator();
				while (processorIterator.hasNext())
				{
					Processor processor = (Processor) processorIterator.next();
					OutputPort[] ports = processor.getBoundOutputPorts();
					for (int index = 0; index < ports.length; index++)
					{
						ResultSource outputSource = (ResultSource) sourceMap.get(ports[index]);
						if (outputSource != null)
						{
							source.addOutput(outputSource);
							outputSource.addInput(source);
						}
					}
				}
			}

			sources = new ResultSource[sourceMap.size()];
			sourceMap.values().toArray(sources);

			Arrays.sort(sources, new Comparator()
			{
				public boolean equals(Object obj)
				{
					return false;
				}

				public int compare(Object o1, Object o2)
				{
					return ((ResultSource) o1).getDepth() - ((ResultSource) o2).getDepth();
				}
			});
		}
	}

	/**
	 * 
	 *  
	 */
	private void createColumns()
	{
		createSources();
		Collection columnList = new ArrayList();
		int depth = -1;
		ResultTableColumn column = null;
		for (int index = 0; index < sources.length; index++)
		{
			if (condenseColumns)
			{
				if (sources[index].getDepth() != depth)
				{
					ResultTableColumn newColumn = new ResultTableColumn();
					newColumn.previousColumn = column;
					if (column != null)
					{
						column.nextColumn = newColumn;
					}
					column = newColumn;
					depth = sources[index].getDepth();
					columnList.add(column);
				}
			}
			else
			{
				ResultTableColumn newColumn = new ResultTableColumn();
				newColumn.previousColumn = column;
				column.nextColumn = newColumn;
				column = newColumn;
				columnList.add(column);
			}
			column.addSource(sources[index]);
		}
		columns = new ResultTableColumn[columnList.size()];
		columnList.toArray(columns);
		update();
	}

	/**
	 * 
	 * @param provenanceXML
	 *            the provenance as a chunk of almost rdf
	 * @return a HashMap with the keys being lsids, and the values being an
	 *         ArrayList of lsids that contributed to creating the key lsid.
	 * @throws JDOMException
	 * @throws IOException
	 */
	private HashMap parseProvenance(String provenanceXML) throws JDOMException, IOException
	{
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new StringReader(provenanceXML));
		Element root = doc.getRootElement();
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
			int row = 0;
			for (int index = columns.length - 1; index > 0; index--)
			{
				row = columns[index].fillColumn(row);
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
				if (result != null)
				{
					getAllInputs(thing, inputLSIDs);
					return result;
				}
			}
		}
		return null;
	}
}
