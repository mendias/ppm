package at.ppm.puppet.dal;

import java.util.ArrayList;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import at.ppm.puppet.dal.hibpojos.Assignment;
import at.ppm.puppet.dal.hibpojos.ModuleVersion;
import at.ppm.puppet.dal.hibpojos.Node;
import at.ppm.puppet.dal.utils.HibernateSession;

public class ModelRepositoryImpl implements IModelRepository {
	/**
	 * 
	 */
	
	@Override
	public ArrayList<String> getDependenciesFromModule(String checkBoxName) {
		ArrayList<String> list = new ArrayList<String>();
		Session session = HibernateSession.getInstance();
		Query query = session.createQuery("select parent from Dependency as dep where dep.module = " + "'" + checkBoxName + "'");
		list = (ArrayList<String>) query.list();
		HibernateSession.closeSession();
		return list;
		
	};
	
	
	
	
	
	@Override
	public void addAssignmentToNode(Node node, Assignment assignment,
			ModuleVersion module) {
		Session session = HibernateSession.getInstance();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(node);
			session.update(module);
			session.save(assignment);
			tx.commit();
		} catch (Exception e) {
			rollBack(tx, e);
		} finally {
			HibernateSession.closeSession();
		}

	}

	@Override
	// HQL for deleting
	public void deleteNodeAndAssignments(Node node) {
		Session session = HibernateSession.getInstance();
		Transaction tx = null;
		ArrayList<Assignment> assignments = node.getAssignment();
		try {
			tx = session.beginTransaction();
			if (assignments.size() > 0) {
				for (Assignment a : assignments) {
					session.delete(a);
				}
			}
			session.delete(node);
			tx.commit();

		} catch (HibernateException e) {
			rollBack(tx, e);
		} finally {
			HibernateSession.closeSession();
		}
	}

	@Override
	public void deleteAssignment(Assignment assignment) {
		Session session = HibernateSession.getInstance();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(assignment);
			tx.commit();
		} catch (HibernateException e) {
			rollBack(tx, e);
		} finally {
			HibernateSession.closeSession();
		}
	}

	@Override
	public ArrayList<Assignment> getAssignmentsFromNode(Node node) {
		return node.getAssignment();
	}

	@Override
	public boolean create(Object object) {
		Session session = HibernateSession.getInstance();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(object);
			tx.commit();

		} catch (Exception e) {
			rollBack(tx, e);
		} finally {
			HibernateSession.closeSession();
		}
		return true;
	}

	@Override
	public void update(Object obj) {
		Session session = HibernateSession.getInstance();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(obj);
			tx.commit();

		} catch (HibernateException e) {
			rollBack(tx, e);
		} finally {
			HibernateSession.closeSession();
		}

	}

	private void rollBack(Transaction tx, Exception e) {
		if (tx != null) {
			tx.rollback();
			e.printStackTrace();
		}
	}

	@Override
	public Node getNode(int id) {
		Session session = HibernateSession.getInstance();
		Node node = (Node) session.get(Node.class, id);
		HibernateSession.closeSession();
		return node;
	}

	@Override
	public <T> ArrayList<T> getAll(Class<T> clazz) {
		Session session = HibernateSession.getInstance();
		// Criteria criteria = session.createCriteria(clazz);
		Query query = session.createQuery("from " + clazz.getName());
		ArrayList<T> list = (ArrayList<T>) query.list();
		HibernateSession.closeSession();
		return list;
	}

	@Override
	public ModuleVersion getModuleVersion(int id) {
		Session session = HibernateSession.getInstance();
		ModuleVersion module = (ModuleVersion) session.get(ModuleVersion.class,
				id);
		HibernateSession.closeSession();
		return module;
	}
}
