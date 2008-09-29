package uk.org.mygrid.taverna.processors;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.junit.Test;

public class  GridSamJSDLWorkerTest  extends LocalWorkerTestCase
{
	
	@Override
	protected String[] expectedInputNames() {
		return new String[]{"executable","arguments", "myproxyUsername", "myproxyPhrase","outputFile"};
	}

	@Override
	protected String[] expectedInputTypes() {
		return new String[]{"'text/plain'","'text/plain'","'text/plain'","'text/plain'","'text/plain'" };
	}

	@Override
	protected String[] expectedOutputNames() {
		// TODO Auto-generated method stub
		return new String[]{"output", "time"};
	}

	@Override
	protected String[] expectedOutputTypes() {
		return new String[]{"'text/plain'", "'text/plain'"};
		
	}

	@Override
	protected LocalWorker getLocalWorker() {
		// TODO Auto-generated method stub
		return new GridSamJSDLWorker();
	}
	
		 
	@Test
	public void testJSDL() throws Exception {
		GridSamJSDLWorker worker = (GridSamJSDLWorker)this.getLocalWorker();
		Map<String,DataThing> inputs = new HashMap<String, DataThing>();
		inputs.put("executable",new DataThing("/bin/echo"));
		inputs.put( "arguments", new DataThing("HelloWorld"));
		inputs.put("myproxyUsername", new DataThing("zzalsbk2"));
		inputs.put("myproxyPhrase", new DataThing("abc1234"));
		inputs.put("outputFile", new DataThing("zzalsbk2file"));	
		
		Map<String,DataThing> workerOutput = worker.execute(inputs);
		DataThing dataThing = (DataThing) workerOutput.get("time");
		String timeStr =(String) dataThing.getDataObject();
		
		StringBuffer jsdl=new StringBuffer("<submitJob xmlns=\"http://www.icenigrid.org/service/gridsam\">");
		
		jsdl.append("<JobDescription><JobDefinition xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl\">");
		jsdl.append("<JobDescription>");
		jsdl.append("<Application>");
		jsdl.append("<POSIXApplication xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl-posix\">");
 		jsdl.append("<Executable>/bin/echo</Executable>");
		jsdl.append("<Argument>HelloWorld</Argument>");
		jsdl.append("<Output>"+"zzalsbk2"+timeStr+"</Output>");
		jsdl.append("</POSIXApplication>");
		jsdl.append("</Application>");
		jsdl.append("<DataStaging>");
		jsdl.append("<FileName>"+"zzalsbk2"+timeStr +"</FileName>");
		jsdl.append("<CreationFlag>overwrite</CreationFlag>");
		jsdl.append("<Target>");
		jsdl.append("<URI>ftp://rpc326.cs.man.ac.uk:19245/zzalsbk2file</URI>");
		jsdl.append("</Target>");
		jsdl.append("</DataStaging>");
		jsdl.append("</JobDescription>");
		jsdl.append("<MyProxy xmlns=\"urn:gridsam:myproxy\">");
		jsdl.append("<ProxyServer>myproxy.grid-support.ac.uk</ProxyServer>");
		jsdl.append("<ProxyServerDN>/C=UK/O=eScience/OU=CLRC/L=DL/CN=host/myproxy.grid-support.ac.uk/E=a.j.richards@dl.ac.uk</ProxyServerDN>");
		jsdl.append("<ProxyServerPort>7512</ProxyServerPort>");
		jsdl.append("<ProxyServerUserName>zzalsbk2</ProxyServerUserName>");
		jsdl.append("<ProxyServerPassPhrase>abc1234</ProxyServerPassPhrase>");
		jsdl.append("<ProxyServerLifetime>7512</ProxyServerLifetime>");
		jsdl.append("</MyProxy>");
		jsdl.append("</JobDefinition></JobDescription></submitJob>");
		
		dataThing = (DataThing) workerOutput.get("output");
		String outputStr =(String) dataThing.getDataObject();
		outputStr = outputStr.replaceAll("\t", ""); 
		outputStr = outputStr.replaceAll("\n", "");
	
		System.out.println (outputStr);
		String jsdlStr = jsdl.toString();
		System.out.println(jsdlStr);
		if (!this.compareStrings(outputStr, jsdlStr))
			fail("I need to write a test!"); 
	}

	
	private boolean compareStrings(String str1, String str2){
		//boolean areEqual = false;
		int len1, len2;
		len1 = str1.length();
		len2 = str2.length();
		CharSequence seq1 = (CharSequence)str1;
		CharSequence seq2 = (CharSequence)str2;
		
		int len = java.lang.Math.min(len1, len2);	
		for (int i=0; i <len; i++){
			//System.out.println("chars at "+i +" "+seq1.charAt(i)+" "+seq2.charAt(i));
			if (!(seq1.charAt(i)==seq2.charAt(i))){
			//	System.out.println("Strings not equal at index:"+i);
				return false;
			}		
		}
		if (!(len1==len2)) {
			System.out.println("different lengths len1:"+len1+" and len2:"+len2);	
			return false;
		}	
		return true;
	}
	
}
