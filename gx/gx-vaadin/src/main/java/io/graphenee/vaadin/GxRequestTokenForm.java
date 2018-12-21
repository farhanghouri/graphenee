package io.graphenee.vaadin;

import org.vaadin.viritin.fields.MTextField;

import com.vaadin.ui.FormLayout;

import io.graphenee.core.model.bean.GxRequestTokenBean;

@SuppressWarnings("serial")
public class GxRequestTokenForm extends TRAbstractForm<GxRequestTokenBean> {

	private MTextField userName;

	@Override
	protected boolean eagerValidationEnabled() {
		return true;
	}

	@Override
	protected void addFieldsToForm(FormLayout form) {
		userName = new MTextField("User Name").withRequired(true);
		form.addComponent(userName);
	}

	@Override
	public String getSaveCaption() {
		return "Send Token";
	}

	@Override
	protected String formTitle() {
		return "Request Token";
	}

	@Override
	protected String popupHeight() {
		return "190px";
	}

	@Override
	protected String popupWidth() {
		return "450px";
	}

	@Override
	protected boolean isPopupResizable() {
		return false;
	}

}
