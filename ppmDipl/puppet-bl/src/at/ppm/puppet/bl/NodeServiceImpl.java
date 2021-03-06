package at.ppm.puppet.bl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import at.ppm.puppet.bl.Interfaces.INodeService;
import at.ppm.puppet.bl.util.PropertyConfig;
import at.ppm.puppet.bl.util.PropertyConfig.PropertyFile;
import at.ppm.puppet.dal.IModelRepository;
import at.ppm.puppet.dal.ModelRepositoryImpl;
import at.ppm.puppet.dal.hibpojos.Assignment;
import at.ppm.puppet.dal.hibpojos.ModuleVersion;
import at.ppm.puppet.dal.hibpojos.Node;

public class NodeServiceImpl implements INodeService{
	
	private final IModelRepository repo = new ModelRepositoryImpl();
	private final Preferences prefs = InstanceScope.INSTANCE.getNode("at.ppm.puppet.blNodes");
	private final PropertyConfig modulConfig = new PropertyConfig(PropertyFile.MODUL);

	@Override
	public ArrayList<Node> getAllNodes() {
		return repo.getAll(Node.class);
	}

	@Override
	public Node getNode(String nodeName) {
		int nodeId = prefs.getInt(nodeName, 0);
		return repo.getNode(nodeId);
	}

	@Override
	public void changeAssignmentState(Node node, Assignment assignment,
			AssignmentState state) {
		assignment.setState(state.toString());
		repo.update(assignment);
		repo.update(node);
	}

//	@Override
//	public void removeAssignmentFromNode(String nodeName, String software) {
//		Node node = getNode(nodeName);
//		int versionId = getModuleVersionId(software);
//		ArrayList<Assignment> assignments = node.getAssignment();
//		Iterator<Assignment> iterator = assignments.iterator();
//		while (iterator.hasNext()) {
//			Assignment assignment = iterator.next();
//			if (assignment.getModuleVersion().getModule_version_id() == versionId) {
//				repo.deleteAssignment(assignment);
//				iterator.remove();
//				break;
//			}
//		}
//		repo.update(node);
//		
//	}
	
	private int getModuleVersionId(String software) {
		return repo.getModuleVersion(modulConfig.get(software))
				.getModule_version_id();
	}

	@Override
	public void createNode(String nodeName) {
		if (nodeName == null || !(nodeName.trim().length() > 0)) {
			// TODO handle this one
			return;
		}
		try {
			if (Arrays.asList(prefs.keys()).contains(nodeName)) {
				throw new IllegalArgumentException("Node already exists");
			}
			Node node = new Node();
			node.setName(nodeName);
			if (repo.create(node)) {
				prefs.putInt(node.getName(), node.getNode_id());
				prefs.flush();
			}

		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void removeAssignment(Assignment assignment) {
		repo.deleteAssignment(assignment);
	}

	@Override
	public boolean nodeExists(String nodeName) {
		if (prefs.getInt(nodeName, 0) != 0)	{return true;}
		return false;
	}

	@Override
	public void deleteNodeAndAssignments(String nodeName) {
		Node node = getNode(nodeName);
		repo.deleteNodeAndAssignments(node);
		prefs.remove(nodeName);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	@Override
	public void addAssignmentToNode(String nodeName, String moduleNameFromCheckBox) {
		Node node = getNode(nodeName);
		if (nodeContainsSoftware(nodeName, moduleNameFromCheckBox)) {
			throw new IllegalArgumentException(
					"Node already contains this Module");
		}
		Assignment assignment = new Assignment();
		assignment.setNode(node);
		assignment.setDate(new Date());
		assignment.setState(AssignmentState.INSTALLING.toString());
		ModuleVersion moduleVersion = repo.getModuleVersion(modulConfig.get(moduleNameFromCheckBox));
		assignment.setModuleVersion(moduleVersion);
		repo.addAssignmentToNode(node, assignment, moduleVersion);
		
	}
	
	public boolean nodeContainsSoftware(String nodeName, String software) {
		Node node = getNode(nodeName);
		ArrayList<Assignment> assignments = node.getAssignment();
		int versionId = getModuleVersionId(software);
		for (Assignment assignment : assignments) {
			if (assignment.getModuleVersion().getModule_version_id() == versionId) {
				return true;
			}
		}
		return false;

	}

	@Override
	public Assignment getAssignmentFromNode(Node node, String software) {
		int versionId = getModuleVersionId(software);
		ArrayList<Assignment> assignments = node.getAssignment();
		Iterator<Assignment> iterator = assignments.iterator();
		while (iterator.hasNext()) {
			Assignment assignment = iterator.next();
			if (assignment.getModuleVersion().getModule_version_id() == versionId) {
				return assignment;
			}
		}
		return null;
	}

	@Override
	public String getNodeState(ArrayList<Assignment> assignments) {
		String stateOrange = null;
		String stateGreen = null;
		for (Assignment assignment : assignments) {
			if (assignment.getState().equalsIgnoreCase(AssignmentState.INSTALLING.toString())) {
				stateOrange = "orange.jpeg";
			}
			if (assignment.getState().equalsIgnoreCase(AssignmentState.INSTALLED.toString())) {
				stateGreen = "green.jpeg";
			}
			if (assignment.getState().equalsIgnoreCase(AssignmentState.UNINSTALL.toString())) {
				stateOrange = "orange.jpeg";
			}
			if (assignment.getState().equalsIgnoreCase(AssignmentState.FAILED.toString())) {
				return "red.jpeg";
			}
		}
		return stateOrange == null ? stateGreen : stateOrange;
	}

	//TODO check for case problems
	@Override
	public void deleteNodeLogFile(String name) {
		File logFile = new File("/etc/puppet/logs/" + name + ".txt");
		File nodesConfigFile = new File("/etc/puppet/manifests/" + name + ".pp");
		logFile.setWritable(true);
		nodesConfigFile.setWritable(true);
		logFile.delete();
		nodesConfigFile.delete();
//		String s = f.getAbsolutePath();
//		try {
//		    java.lang.Runtime.getRuntime().exec("rm -f " + f.getAbsolutePath());
//		} catch (IOException e) {
//		    e.printStackTrace();
//		}  
		
	}

}
