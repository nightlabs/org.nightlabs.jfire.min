/*
 * Created 	on Dec 1, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit.blockbased;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nightlabs.jfire.base.person.PersonStructProvider;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.person.PersonStructBlock;
import org.nightlabs.jfire.person.id.PersonStructBlockID;

/**
 * Registry for PersonStructBlocks to be displayed by 
 * block-based PersonEditors.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonEditorStructBlockRegistry {
	
	/**
	 * key: String editorScope<br/>
	 * value: Map editorsStructBlocks<br/>
	 *	-> key: String editorName<br/>
	 *  -> value: List 
	 * 				-> value: PersonStructBlockID personStructBlockKey
	 */
	private Map editorStructBlocks = new HashMap();
	
	/**
	 * Adds the list of personStructBlockKeys to
	 * the editor with the given scope and name.
	 *  
	 * @param editorScope
	 * @param editorName
	 * @param personStructBlockKeys
	 */
	public void addEditorStructBlocks(String editorScope, String editorName, PersonStructBlockID[] personStructBlockIDs) {
		if (!editorStructBlocks.containsKey(editorScope)) {
			editorStructBlocks.put(editorScope,new HashMap());
		}
		Map editorsStructBlockKeys = (Map)editorStructBlocks.get(editorScope);
		if (!editorsStructBlockKeys.containsKey(editorName))
			editorsStructBlockKeys.put(editorName,new LinkedList());
		List personStructBlockKeyList = (List)editorsStructBlockKeys.get(editorName);
		for (int i=0; i<personStructBlockIDs.length; i++) {
			personStructBlockKeyList.add(personStructBlockIDs[i]);
		}
	}
	
	/**
	 * Returns an Iterator to a list of Strings representing the
	 * personStructBlockKeys registered for the editor in scope
	 * editorScope and with name editorName. 
	 * 
	 * @param editorScope
	 * @param editorName
	 * @return
	 */
	public List getEditorStructBlocks(String editorScope, String editorName) {
		Map editorsStructBlockKeys = (Map)editorStructBlocks.get(editorScope);
		if (editorsStructBlockKeys != null) {
			List personStructBlockKeyList = (List)editorsStructBlockKeys.get(editorName);
			if (personStructBlockKeyList != null) {
				return personStructBlockKeyList;
			}
			else
				return Collections.EMPTY_LIST;
		}
		else
			return Collections.EMPTY_LIST;
	}
	
	public Iterator getEditorStructBlocksIterator(String editorScope, String editorName) {
		return getEditorStructBlocks(editorScope,editorName).iterator();
	}
	
	/**
	 * Checks weather the given PersonStructBlock is registered in
	 * at least one editor within the given scope.
	 * 
	 * @param editorScope
	 * @param structBlock
	 * @return
	 */
	public boolean hasEditorCoverage(String editorScope, PersonStructBlock structBlock) {
		Map editorsStructBlockKeys = (Map)editorStructBlocks.get(editorScope);
		if (editorsStructBlockKeys != null) {
			for (Iterator it = editorsStructBlockKeys.values().iterator(); it.hasNext(); ) {
				List personStructBlockKeyList = (List)it.next();
				if (personStructBlockKeyList.contains(PersonStructBlockID.create(structBlock.getPersonStructBlockOrganisationID(),structBlock.getPersonStructBlockID())))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns a list of Strings representing all PersonStructBlock not 
	 * covered by other editors within the given editorScope.
	 * 
	 * @param editorScope
	 * @param editorName
	 * @return
	 */
	public List computeRemainingBlockKeyList(String editorScope) {
		List result  = new LinkedList();
		PersonStruct structure = PersonStructProvider.getPersonStructure();
		for (Iterator it = structure.getPersonStructBlocks().iterator(); it.hasNext(); ) {
			PersonStructBlock structBlock = (PersonStructBlock)it.next();
			if (!hasEditorCoverage(editorScope,structBlock))
				result.add(PersonStructBlockID.create(structBlock.getPersonStructBlockOrganisationID(), structBlock.getPersonStructBlockID()));
		}
		return result;
	}
	
	public PersonStructBlockID[] computeRemainingBlockKeys(String editorScope) {
		List keys = computeRemainingBlockKeyList(editorScope);
		PersonStructBlockID[] result = new PersonStructBlockID[keys.size()];
		for (int i=0; i<keys.size(); i++) {
			result[i] = (PersonStructBlockID)keys.get(i);
		}
		return result;
	}
	
	private static PersonEditorStructBlockRegistry sharedInstance;
	public static PersonEditorStructBlockRegistry sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new PersonEditorStructBlockRegistry();
		return sharedInstance;
	}
	
}
