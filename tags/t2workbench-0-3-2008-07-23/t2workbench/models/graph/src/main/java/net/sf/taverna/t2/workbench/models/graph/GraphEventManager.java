package net.sf.taverna.t2.workbench.models.graph;

public interface GraphEventManager {

	public abstract void mouseClicked(final GraphElement graphElement,
			short button, boolean altKey, boolean ctrlKey, boolean metaKey,
			final int x, final int y, int screenX, int screenY);

	public abstract void mouseDown(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY);

	public abstract void mouseUp(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, final int x,
			final int y, int screenX, int screenY);

	public abstract void mouseMoved(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY);

}