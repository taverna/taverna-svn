package net.sf.taverna.t2.workbench.edits.impl.menu;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;

/**
 * A section of the Edit menu that contains {@link UndoMenuSection undo} and
 * {@link RedoMenuAction redo}.
 * 
 * @author Stian Soiland-Reyes
 */
public class UndoMenuSection extends AbstractMenuSection {

	public static final URI UNDO_SECTION_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/edits#undoSection");
	private static final URI EDIT_MENU_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#edit");

	public UndoMenuSection() {
		super(EDIT_MENU_URI, 20, UNDO_SECTION_URI);
	}
}
