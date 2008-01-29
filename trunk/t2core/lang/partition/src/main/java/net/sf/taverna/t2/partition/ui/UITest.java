package net.sf.taverna.t2.partition.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;

import net.sf.taverna.t2.partition.PartitionAlgorithmSPI;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class UITest extends JFrame {

	private static final long serialVersionUID = -734851883737477053L;

	public UITest() {
		super();
		getContentPane().setLayout(new BorderLayout());
		List<PartitionAlgorithmSPI<?>> paList = new ArrayList<PartitionAlgorithmSPI<?>>();
		paList.add(new LiteralValuePartitionAlgorithm());
		paList.add(new LiteralValuePartitionAlgorithm());
		paList.add(new LiteralValuePartitionAlgorithm());
		PartitionAlgorithmListEditor pale = new PartitionAlgorithmListEditor(paList);
		getContentPane().add(pale, BorderLayout.NORTH);
		setVisible(true);
		
	}
	
	public static void main(String[] args) {
		JLabel l = new JLabel("Foo");
		System.out.println(l.getPreferredSize());
		System.out.println(l.getWidth());
		
		new UITest();
	}
	
}
