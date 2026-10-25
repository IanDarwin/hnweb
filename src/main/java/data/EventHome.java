package data;

import jakarta.ejb.Stateful;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import entity.EntityHome;
import model.Event;
import webtier.WebConstants;

@Stateful @Named @ConversationScoped
public class EventHome extends EntityHome<Event,Long> {

	@Inject public EventDAO eventDAO;
	
	private static final long serialVersionUID = 1L;

	static final String LIST_PAGE = "EventList";
	
	public EventHome() {
		System.out.println("EventHome.EventHome()");
	}

	@Override
	public Event newInstance() {
		return new Event();
	}

	@Override
	public String getListPage() {
		return LIST_PAGE;
	}

	public void wire(String s) {
		if (s == null || s.isEmpty()) {
			instance = newInstance();
			return;
		}
		wire(Long.parseLong(s));
	}

	/**
	 * The id of the event being edited, carried in a hidden field of the edit forms.
	 * The edit page is two requests (GET to show it, POST to save it) and this bean may be
	 * rebuilt blank for the POST, so the form itself must say which event it is about.
	 */
	private Long postedId;

	public Long getEditId() {
		return (instance != null && instance.getId() != null) ? instance.getId() : postedId;
	}

	public void setEditId(Long editId) {
		this.postedId = editId;
	}

	/**
	 * Save from the edit page: create a new event, or update the existing one.
	 * @return The list page, or null (stay on the page) if the event no longer exists.
	 */
	@Transactional
	public String saveEvent() {
		final Long id = getEditId();
		if (id == null) {
			eventDAO.create(instance);
		} else {
			// Copy the form's fields onto the stored event, so fields that are not on
			// the form (e.g., "times") keep their values.
			Event stored = eventDAO.findById(id);
			if (stored == null) {
				return fail("That event no longer exists; it may have been deleted.");
			}
			stored.setDate(instance.getDate());
			stored.setStartTime(instance.getStartTime());
			stored.setEndTime(instance.getEndTime());
			stored.setTitle(instance.getTitle());
			stored.setLocation(instance.getLocation());
			stored.setEventAbstract(instance.getEventAbstract());
			stored.setSpeakerName(instance.getSpeakerName());
			stored.setSpeakerBio(instance.getSpeakerBio());
			eventDAO.update(stored);
		}
		return LIST_PAGE + WebConstants.FORCE_REDIRECT;
	}

	/** Delete from the edit page. */
	@Transactional
	public String deleteEvent() {
		final Long id = getEditId();
		if (id == null) {
			return fail("This event hasn't been saved yet, so there is nothing to delete.");
		}
		return removeById(id);
	}

	/** Delete by id. The event is looked up first: removing a detached instance fails. */
	@Transactional
	public String removeById(Long id) {
		Event stored = eventDAO.findById(id);
		if (stored != null) {
			System.out.println("EventHome: Remove " + stored);
			eventDAO.delete(stored);
		}
		return LIST_PAGE + WebConstants.FORCE_REDIRECT;
	}

	@Transactional
	public String remove(Event evt) {
		return removeById(evt.getId());
	}

	private String fail(String message) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
		return null;
	}
}
