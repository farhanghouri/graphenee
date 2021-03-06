/*******************************************************************************
 * Copyright (c) 2016, 2018 Farrukh Ijaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package io.graphenee.vaadin;

import javax.annotation.PostConstruct;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import io.graphenee.vaadin.event.DashboardEvent.UserLoginRequestedEvent;
import io.graphenee.vaadin.event.DashboardEventBus;

@SuppressWarnings("serial")
public abstract class VaadinAbstractLoginComponent extends LoginForm {

	public VaadinAbstractLoginComponent() {
		if (!isSpringComponent()) {
			postConstruct();
		}
	}

	protected boolean isSpringComponent() {
		return this.getClass().getAnnotation(SpringComponent.class) != null;
	}

	@PostConstruct
	private void postConstruct() {
		postInitialize();
	}

	public Component build(TextField userNameField, PasswordField passwordField, Button loginButton) {
		VerticalLayout mainLayout = null;
		if (mainLayout == null) {
			mainLayout = new VerticalLayout();
			mainLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
			addLoginListener(listener -> {
				UserLoginRequestedEvent userLoginRequestedEvent = new UserLoginRequestedEvent(listener.getLoginParameter("username"), listener.getLoginParameter("password"));
				DashboardEventBus.sessionInstance().post(userLoginRequestedEvent);
			});
			setSizeFull();
			Component loginForm = buildLoginForm(userNameField, passwordField, loginButton);
			mainLayout.setSizeFull();
			mainLayout.addComponent(loginForm);
			postBuild();
		}
		return mainLayout;
	}

	protected void postBuild() {
	}

	private Component buildLoginForm(TextField userNameField, PasswordField passwordField, Button loginButton) {
		final VerticalLayout loginPanel = new VerticalLayout();
		loginPanel.setSizeUndefined();
		loginPanel.setSpacing(false);
		loginPanel.setMargin(false);
		Responsive.makeResponsive(loginPanel);
		loginPanel.addStyleName("login-panel");
		loginPanel.addComponent(buildLabels());
		loginPanel.addComponent(buildFields(userNameField, passwordField, loginButton));

		Button forgotPasswordButton = new Button("Forgot Password");
		forgotPasswordButton.setStyleName(ValoTheme.BUTTON_LINK);
		forgotPasswordButton.setStyleName(ValoTheme.BUTTON_BORDERLESS, true);
		forgotPasswordButton.addClickListener(event -> {
			DashboardEventBus.sessionInstance().post("");
		});

		loginPanel.addComponent(forgotPasswordButton);
		if (isRememberMeEnabled()) {
			loginPanel.addComponent(new CheckBox("Remember me", true));
		}
		return loginPanel;
	}

	protected boolean isRememberMeEnabled() {
		return false;
	}

	private Component buildLabels() {
		CssLayout labels = new CssLayout();
		labels.addStyleName("labels");

		if (dashboardSetup().loginFormLogo() != null) {
			Image logoImage = dashboardSetup().loginFormLogo();
			logoImage.addStyleName("logo-image");
			logoImage.setHeight(logoImageHeight());
			labels.addComponent(logoImage);
		} else {
			Label welcome = new Label("Welcome");
			welcome.setSizeUndefined();
			welcome.addStyleName(ValoTheme.LABEL_H4);
			welcome.addStyleName(ValoTheme.LABEL_COLORED);
			labels.addComponent(welcome);
		}

		Label title = new Label(dashboardSetup().loginFormTitle());
		title.setSizeUndefined();
		title.addStyleName(ValoTheme.LABEL_H3);
		labels.addComponent(title);
		return labels;
	}

	protected String logoImageHeight() {
		return "50px";
	}

	private Component buildFields(TextField userNameField, PasswordField passwordField, Button loginButton) {
		HorizontalLayout fields = new HorizontalLayout();
		fields.setSpacing(true);
		fields.addStyleName("fields");

		userNameField.setIcon(FontAwesome.USER);
		userNameField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);

		passwordField.setIcon(FontAwesome.LOCK);
		passwordField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);

		loginButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		loginButton.setClickShortcut(KeyCode.ENTER);
		userNameField.focus();

		fields.addComponents(userNameField, passwordField, loginButton);
		fields.setComponentAlignment(loginButton, Alignment.BOTTOM_LEFT);

		// signin.addClickListener(new ClickListener() {
		// @Override
		// public void buttonClick(final ClickEvent event) {
		// DashboardEventBus.sessionInstance().post(new
		// UserLoginRequestedEvent(username.getValue(), password.getValue()));
		// }
		// });
		return fields;
	}

	@Override
	protected Component createContent(TextField userNameField, PasswordField passwordField, Button loginButton) {
		userNameField.setCaption("Username");
		passwordField.setCaption("Password");
		loginButton.setCaption("Login");
		return build(userNameField, passwordField, loginButton);
	}

	protected Component createCustomLayout() {
		return null;
	}

	@Override
	public void setContent(Component content) {
		Component c = createCustomLayout();
		if (c == null) {
			super.setContent(content);
		} else {
			super.setContent(c);
		}
	}

	protected abstract AbstractDashboardSetup dashboardSetup();

	protected void postInitialize() {
	}

}
