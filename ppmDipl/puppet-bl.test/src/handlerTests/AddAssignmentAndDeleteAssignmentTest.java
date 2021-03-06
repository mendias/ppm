//package handlerTests;
//
//import java.util.ArrayList;
//
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import testUtil.TestConstants;
//import at.ppm.puppet.bl.DeploymentConfigService;
//import at.ppm.puppet.dal.hibpojos.Node;
//
//public class AddAssignmentAndDeleteAssignmentTest {
//	private final String NODENAME = TestConstants.NODENAME;
//	private final String JAVA = TestConstants.JAVA;
//	private final String NOTEPAD = TestConstants.NOTEPAD;
//
//	@Before
//	public void before() {
//		cleanDb();
//		DeploymentConfigService.createNode(NODENAME);
//	}
//
//	
//	@Test
//	public void addAssignmentAndDelete(){
//		
//		DeploymentConfigService.addAssignmentToNode(NODENAME, JAVA);
//		DeploymentConfigService.addAssignmentToNode(NODENAME, NOTEPAD);
//		int assSize = DeploymentConfigService.getNode(NODENAME).getAssignment().size();
//		Assert.assertEquals(2, assSize);
//		Assert.assertTrue(DeploymentConfigService.nodeContainsSoftware(NODENAME, JAVA));
//		Assert.assertTrue(DeploymentConfigService.nodeContainsSoftware(NODENAME, NOTEPAD));
//		
//		DeploymentConfigService.removeAssignmentFromNode(NODENAME, JAVA);
//		assSize = DeploymentConfigService.getNode(NODENAME).getAssignment().size();
//		Assert.assertEquals(1, assSize);
//		Assert.assertFalse(DeploymentConfigService.nodeContainsSoftware(NODENAME, JAVA));
//		
//	}
//
//	private void cleanDb() {
//		ArrayList<Node> allNodes = DeploymentConfigService.getAllNodes();
//		for (Node node : allNodes) {
//			DeploymentConfigService.deleteNodeAndAssignments(node.getName());
//		}
//	}
//	
//	@After
//	public void after() {
//		cleanDb();
//	}
//
//}
