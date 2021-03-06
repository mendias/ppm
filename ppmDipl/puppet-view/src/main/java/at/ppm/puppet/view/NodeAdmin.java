package at.ppm.puppet.view;

import java.util.ArrayList;
import java.util.Iterator;

import javafx.embed.swt.FXCanvas;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import at.ppm.puppet.bl.DeploymentConfigService;
import at.ppm.puppet.bl.PuppetModule;
import at.ppm.puppet.bl.SoftwareModule;
import at.ppm.puppet.bl.Interfaces.INodeService.AssignmentState;
import at.ppm.puppet.dal.hibpojos.Node;
import at.ppm.view.util.Events;

public class NodeAdmin {
	
	private ArrayList<SoftwareModule> treeStructureList;
	private ArrayList<PuppetModule> puppetModulesFromSelectedNode;
	private TreeView<String> treeView;
	private ArrayList<CheckBoxTreeItem<String>> checkBoxRoots = new ArrayList<CheckBoxTreeItem<String>>();
	private FXCanvas canvas;
	private Node lastSelectedNode;
	private ArrayList<CheckBox> boxes;
	private Button deploy;
	@Inject
	IEventBroker eventBroker;
	
	public NodeAdmin() {
	}

	/**
	 * Create contents of the view part.
	 */
	@PostConstruct
	public void createControls(Composite parent) {
		canvas = new FXCanvas(parent, SWT.NONE);
		deploy = new Button("Deploy");
		createTree(canvas);
	}

	private void createTree(FXCanvas canvas) {
		boxes = new ArrayList<>();
		BorderPane root = new BorderPane();
		treeView = createTreeStructure();
		root.setCenter(treeView);
		deploy.setOnAction(new MyDeployHandler());
		root.setBottom(deploy);
//		root.getChildren().add(createTreeStructure());
		Scene scene = new Scene(root);
		canvas.setScene(scene);
	}

	/**
	 * creates the treeStructure. 
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private TreeView<String> createTreeStructure() {
		//contains all packages with versions
		treeStructureList = DeploymentConfigService.createTreeStructureForJavaFx();
		TreeView<String> treeView = new TreeView<String>();
		if (puppetModulesFromSelectedNode == null) {
			CheckBoxTreeItem<String> root = new CheckBoxTreeItem<String>("Select Node to enable Selecting Software!!");
			treeView.setRoot(root);
			deploy.setDisable(true);
			return treeView;
		}
		deploy.setDisable(false);
		CheckBoxTreeItem<String> root = new CheckBoxTreeItem<String>("Available Software:");
		root.setExpanded(true);
		for (int i = 0; i < treeStructureList.size(); i++) {
			
			if ( !(puppetModulesFromSelectedNode.get(0).getSoftwareVersion().equalsIgnoreCase("Nothing Installed"))) {
				//how many versions of the packet are available
				int numberOfVersions = treeStructureList.get(i).getVersions().size();
				//java for example, this is the expandable treeitem
				CheckBoxTreeItem<String> item = new CheckBoxTreeItem<String>(treeStructureList.get(i).getName());
				checkBoxRoots.add(item);
				@SuppressWarnings("rawtypes")
				TreeItem[] boxArray = new TreeItem[numberOfVersions];
				
				for (int j = 0; j < numberOfVersions; j++) {
					//the version name, for example java1751
					CheckBox checkBox = new CheckBox(treeStructureList.get(i).getVersions().get(j));
					checkBox.setOnAction(new CheckBoxEventHandler());
					boxes.add(checkBox);
					TreeItem<CheckBox> box = new TreeItem<CheckBox>(checkBox);
					if (puppetModulesFromSelectedNode != null) {
						for (int p = 0; p < puppetModulesFromSelectedNode.size(); p++) {
							if ((puppetModulesFromSelectedNode.get(p).getNameAndVersion().equalsIgnoreCase(checkBox.getText())) &&
									(puppetModulesFromSelectedNode.get(p).getState().equalsIgnoreCase(AssignmentState.UNINSTALL.toString()))) {
								checkBox.setSelected(false);
								continue;
							}
							if (puppetModulesFromSelectedNode.get(p).getNameAndVersion().equalsIgnoreCase(treeStructureList.get(i).getVersions().get(j))) {
								checkBox.setSelected(true);
								item.setExpanded(true);
							}
						}
						
					}
					
					boxArray[j] = box;
				}
				item.getChildren().addAll(boxArray);
				root.getChildren().addAll(item);
			}
			else {
				int numberOfVersions = treeStructureList.get(i).getVersions().size();
				if (numberOfVersions == 0) {
					@SuppressWarnings("rawtypes")
					TreeItem[] boxArray = new TreeItem[1];
					CheckBox checkBox = new CheckBox(treeStructureList.get(i).getName());
					checkBox.setOnAction(new CheckBoxEventHandler());
					boxes.add(checkBox);
					TreeItem<CheckBox> box = new TreeItem<CheckBox>(checkBox);
					boxArray[0] = box;
					root.getChildren().addAll(boxArray);
					continue;
				}
				CheckBoxTreeItem<String> item = new CheckBoxTreeItem<String>(treeStructureList.get(i).getName());
				checkBoxRoots.add(item);
				@SuppressWarnings("rawtypes")
				TreeItem[] boxArray = new TreeItem[numberOfVersions];
				
				for (int j = 0; j < numberOfVersions; j++) {
					CheckBox checkBox = new CheckBox(treeStructureList.get(i).getVersions().get(j));
					checkBox.setOnAction(new CheckBoxEventHandler());
					boxes.add(checkBox);
					TreeItem<CheckBox> box = new TreeItem<CheckBox>(checkBox);
					
					boxArray[j] = box;
				}
				item.getChildren().addAll(boxArray);
				root.getChildren().addAll(item);
				
			}
		}
		treeView.setRoot(root);
		return treeView;

	}
	
	//TODO useless????
	private class CheckBoxEventHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {	
			CheckBox clicked = ((CheckBox) event.getSource());
			System.out.println("checked " + clicked.getText());
			if (clicked.isSelected()) {
				checkDbForDependencies(clicked.getText());
			}

		}

		
		private void checkDbForDependencies(String selectedBox) {
			ArrayList<String> dependenciesFromModule = DeploymentConfigService.getDependenciesFromModule(selectedBox);
			if (dependenciesFromModule.size() > 0) {
				for (int i = dependenciesFromModule.size(); i > 0; i--) {
					checkDbForDependencies(dependenciesFromModule.get(i-1));
					Iterator<CheckBox> iter = boxes.iterator();
					while (iter.hasNext()) {
						CheckBox box = iter.next();
						if (box.getText().equalsIgnoreCase(dependenciesFromModule.get(i-1)) && (!box.isSelected())) {
							box.setSelected(true);
							Iterator<CheckBoxTreeItem<String>> rootItemIterator = checkBoxRoots.iterator();
							while (rootItemIterator.hasNext()) {
								CheckBoxTreeItem<String> rootItem = rootItemIterator.next();
								String regex = "(\\d*)(\\W*)";
								String removedNonLitFromBox = box.getText().replaceAll(regex, "");
								if (removedNonLitFromBox.equalsIgnoreCase(rootItem.getValue())) {
									rootItem.setExpanded(true);
								}
							}
						}
					}
				}
			}
			
		}
		
	}
	
	private class MyDeployHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent arg0) {
			DeploymentConfigService.setInstallingStateToAssignments(boxes, lastSelectedNode);
			eventBroker.post(Events.DEPLOINGSTARTED, lastSelectedNode);
		}
		
	}
	
	@Inject
	@Optional
	private void getNotified(@UIEventTopic(Events.NODESELECTED) Node node) {

		lastSelectedNode = node;
	
		if (!(lastSelectedNode instanceof Node) || lastSelectedNode == null) {
			puppetModulesFromSelectedNode = null;
			createTree(canvas);
		}
		else {
			puppetModulesFromSelectedNode = DeploymentConfigService.getPuppetModules(lastSelectedNode);
			if (puppetModulesFromSelectedNode.size() == 0) {
				puppetModulesFromSelectedNode.add(new PuppetModule("Nothing Installed", null, null, null));
			}
					
		}
//		if ((lastSelectedNode == null) || !(node.getName().equals(lastSelectedNode.getName()))) {
			createTree(canvas);			
//		}
		
	}
	



	@PreDestroy
	public void dispose() {
	}

	@Focus
	public void setFocus() {
		// TODO Set the focus to control
	}


}
