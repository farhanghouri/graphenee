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
package io.graphenee.vaadin.view;

import io.graphenee.vaadin.AbstractDashboardSetup;
import io.graphenee.vaadin.VaadinAbstractLoginComponent;

@SuppressWarnings("serial")
public class LoginComponent extends VaadinAbstractLoginComponent {

	private AbstractDashboardSetup dashboardSetup;

	public LoginComponent(AbstractDashboardSetup dashboardSetup) {
		this.dashboardSetup = dashboardSetup;
	}

	@Override
	protected boolean isSpringComponent() {
		return false;
	}

	@Override
	protected AbstractDashboardSetup dashboardSetup() {
		return dashboardSetup;
	}

}
