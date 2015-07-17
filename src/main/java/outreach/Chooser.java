package outreach;

import model.Member;

/** A Visitor pattern for choosing mail recipients */
@FunctionalInterface
public interface Chooser {
	public boolean accept(Member p);
}
