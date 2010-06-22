package org.taverna.server.master;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.taverna.server.master.exceptions.NoCreateException;
import org.taverna.server.master.exceptions.NoDestroyException;
import org.taverna.server.master.exceptions.NoUpdateException;
import org.taverna.server.master.interfaces.TavernaRun;
import org.taverna.server.master.mocks.SimpleServerPolicy;

class MockPolicy extends SimpleServerPolicy {
	MockPolicy() {
		super();
		super.setCleanerInterval(30);
	}
	int maxruns = 10;
	Integer usermaxruns;
	Set<TavernaRun> denyaccess = new HashSet<TavernaRun>();
	boolean exnOnUpdate, exnOnCreate, exnOnDelete;

	@Override
	public int getMaxRuns() {
		return maxruns;
	}

	@Override
	public Integer getMaxRuns(Principal user) {
		return usermaxruns;
	}

	@Override
	public List<SCUFL> listPermittedWorkflows(Principal user) {
		return Arrays.asList();
	}

	@Override
	public boolean permitAccess(Principal user, TavernaRun run) {
		return !denyaccess.contains(run);
	}

	@Override
	public void permitCreate(Principal user, SCUFL workflow)
			throws NoCreateException {
		if (this.exnOnCreate)
			throw new NoCreateException();
	}

	@Override
	public void permitDestroy(Principal user, TavernaRun run)
			throws NoDestroyException {
		if (this.exnOnDelete)
			throw new NoDestroyException();
	}

	@Override
	public void permitUpdate(Principal user, TavernaRun run)
			throws NoUpdateException {
		if (this.exnOnUpdate)
			throw new NoUpdateException();
	}
}