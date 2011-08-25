package org.nightlabs.jfire.prop.cache;

import java.util.Arrays;

/**
 * @author marco
 * @deprecated <b>Internal class!</b> Do not use this class directly! It's only supposed to be used internally!
 */
@Deprecated
public class FetchGroupsPartSet
{
	private String fetchGroups_part1;

	private String fetchGroups_part2;

	private String fetchGroups_part3;

	// If they are not null, it cannot happen, that part1 is empty while there's sth. in part2. Thus, even if a fetch-group
	// has the insane name "null", there is no collision.
	private static final String NULL_IDENTIFIER_PART1 = "";
	private static final String NULL_IDENTIFIER_PART2 = "null";
	private static final String NULL_IDENTIFIER_PART3 = "";

	public FetchGroupsPartSet(String[] fetchGroups) {
		if (fetchGroups == null) {
			fetchGroups_part1 = NULL_IDENTIFIER_PART1;
			fetchGroups_part2 = NULL_IDENTIFIER_PART2;
			fetchGroups_part3 = NULL_IDENTIFIER_PART3;
		}
		else {
			fetchGroups = Arrays.copyOf(fetchGroups, fetchGroups.length);
			Arrays.sort(fetchGroups);

			// already keep this entry here so that we don't need to reconstruct it in getFetchGroups().
			this.fetchGroups = fetchGroups;

			StringBuilder sb = new StringBuilder();
			for (String fetchGroup : fetchGroups) {
				if (fetchGroup.indexOf(':') >= 0)
					throw new IllegalArgumentException("FetchGroup \"" + fetchGroup + "\" contains illegal character ':'!!!");

				if (sb.length() > 0)
					sb.append(":");

				sb.append(fetchGroup);
			}
			int idx = 0;
			fetchGroups_part1 = safeSubstring(sb, idx, (idx += 255));
			fetchGroups_part2 = safeSubstring(sb, idx, (idx += 255));
			fetchGroups_part3 = safeSubstring(sb, idx, Integer.MAX_VALUE);
			if (fetchGroups_part3.length() > 255)
				throw new IllegalArgumentException("Combined fetch-groups are too long!");
		}
	}

	protected String safeSubstring(StringBuilder s, int startInclusive, int endExclusive)
	{
		if (endExclusive > s.length())
			endExclusive = s.length();

		if (startInclusive > endExclusive)
			startInclusive = endExclusive;

		return s.substring(startInclusive, endExclusive);
	}

	public FetchGroupsPartSet(
			String fetchGroups_part1,
			String fetchGroups_part2,
			String fetchGroups_part3
	)
	{
		this.fetchGroups_part1 = fetchGroups_part1;
		this.fetchGroups_part2 = fetchGroups_part2;
		this.fetchGroups_part3 = fetchGroups_part3;
	}

	public String getFetchGroups_part1() {
		return fetchGroups_part1;
	}
	public String getFetchGroups_part2() {
		return fetchGroups_part2;
	}
	public String getFetchGroups_part3() {
		return fetchGroups_part3;
	}

	public boolean isFetchGroupsNull() {
		return NULL_IDENTIFIER_PART1.equals(fetchGroups_part1) && NULL_IDENTIFIER_PART2.equals(fetchGroups_part2);
	}

	private transient String[] fetchGroups;

	public String[] getFetchGroups() {
		if (isFetchGroupsNull())
			return null;

		if (this.fetchGroups == null) {
			String fetchGroups = fetchGroups_part1 + fetchGroups_part2 + fetchGroups_part3;
			this.fetchGroups = fetchGroups.split(":");
		}
		return this.fetchGroups;
	}

//	public static void main(String[] args) {
//		FetchGroupsPartSet fetchGroupsPartSet = new FetchGroupsPartSet(new String[] {
//				"TestBlabla.ksdghfkhsdgfjhsdgfsdhfgsdjgf",
//				"TestBlabla.aaaaaaaaaaabbbbbbbbb",
//				"TestBlabla.ccccccccccccccccccccccccccccc",
//				"Blubbbblubbblubbb.xxxxxxxxxxxxxxhdggfsdzuf",
//				"Adagfjhsgdf.djksuiduisdzdszizfi",
//				"XjksdSXXDSSDSS.sdljkgfkjsdhkjchdsjkhYSS",
//				"Haahaha.sdlkfjhsdkljfospidufousdfiou",
//
//
//				"Blubbbblubbblubbb1.xxxxxxxxxxxxxxhsdzuf",
//				"Adagfjhsgdf1.djksuiduisdzdszizfi",
//				"XjksdSXXDSSDSS1.sdljkgfkjsdhkjchdsjkhYSS",
//				"Haahaha1.sdlkfjhsdkljfospidufousdfiou",
//
//				"Blubbbblubbblubbb2.xxxxxxxxxxxxxxhsdzuf",
//				"Adagfjhsgdf2.djksuiduisdzdszizfi",
//				"XjksdSXXDSSDSS3.sdljkgfkjsdhkjchdsjkhYSS",
//				"Haahaha3.sdlkfjhsdkljfospidufousdfiou",
//		});
//		System.out.println("fetchGroupsPartSet.fetchGroups_part1.length = " + fetchGroupsPartSet.getFetchGroups_part1().length());
//		System.out.println("fetchGroupsPartSet.fetchGroups_part2.length = " + fetchGroupsPartSet.getFetchGroups_part2().length());
//		System.out.println("fetchGroupsPartSet.fetchGroups_part3.length = " + fetchGroupsPartSet.getFetchGroups_part3().length());
//
//		System.out.println("fetchGroupsPartSet.fetchGroups_part1 = " + fetchGroupsPartSet.getFetchGroups_part1());
//		System.out.println("fetchGroupsPartSet.fetchGroups_part2 = " + fetchGroupsPartSet.getFetchGroups_part2());
//		System.out.println("fetchGroupsPartSet.fetchGroups_part3 = " + fetchGroupsPartSet.getFetchGroups_part3());
//	}
}
