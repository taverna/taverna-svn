package net.sf.taverna.t2.activities.beanshell;

import java.io.StringReader;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;
import bsh.ParseException;
import bsh.Parser;

public class BeanshellActivityHealthChecker implements HealthChecker<BeanshellActivity> {

	public boolean canHandle(Object subject) {
		return (subject!=null && subject instanceof BeanshellActivity);
	}

	public HealthReport checkHealth(BeanshellActivity activity) {
		Parser parser = new Parser(new StringReader(activity.getConfiguration().getScript()));
		try {
			while (!parser.Line());
		} catch (ParseException e) {
			return new HealthReport("Beanshell Activity",e.getMessage(),Status.SEVERE);
		}
		return new HealthReport("Beanshell Acitivity","Parsed OK",Status.OK);
	}

}