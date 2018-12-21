package io.graphenee.vaadin;

import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTextField;

import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;

import io.graphenee.core.model.bean.GxVerifyTokenBean;

@SuppressWarnings("serial")
public class GxVerifyTokenForm extends TRAbstractForm<GxVerifyTokenBean> {

	private MTextField token;
	private MButton resendTokenButton;
	private VerifyTokenDelegate delegate;

	@Override
	protected boolean eagerValidationEnabled() {
		return true;
	}

	@Override
	protected void addButtonsToFooter(HorizontalLayout footer) {
		footer.addComponent(resendTokenButton);
	}

	@Override
	protected void addFieldsToForm(FormLayout form) {
		token = new MTextField("Token").withRequired(true);
		resendTokenButton = new MButton("Resend Token");
		resendTokenButton.addClickListener(e -> {
			delegate.onResendTokenClick();
		});
		form.addComponent(token);
	}

	@Override
	public String getSaveCaption() {
		return "Verify";
	}

	@Override
	protected String formTitle() {
		return "Verify Token";
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

	public VerifyTokenDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(VerifyTokenDelegate delegate) {
		this.delegate = delegate;
	}

	public static interface VerifyTokenDelegate {
		public void onResendTokenClick();
	}

}
