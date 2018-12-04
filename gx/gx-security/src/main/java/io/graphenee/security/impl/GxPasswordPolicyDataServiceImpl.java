package io.graphenee.security.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.graphenee.core.exception.ChangePasswordFailedException;
import io.graphenee.core.model.BeanFault;
import io.graphenee.core.model.api.GxDataService;
import io.graphenee.core.model.bean.GxNamespaceBean;
import io.graphenee.core.model.bean.GxPasswordPolicyBean;
import io.graphenee.core.model.bean.GxUserAccountBean;
import io.graphenee.core.model.entity.GxNamespace;
import io.graphenee.core.model.entity.GxPasswordHistory;
import io.graphenee.core.model.entity.GxPasswordPolicy;
import io.graphenee.core.model.entity.GxUserAccount;
import io.graphenee.core.model.jpa.repository.GxNamespaceRepository;
import io.graphenee.core.model.jpa.repository.GxPasswordHistoryRepository;
import io.graphenee.core.model.jpa.repository.GxPasswordPolicyRepository;
import io.graphenee.core.model.jpa.repository.GxUserAccountRepository;
import io.graphenee.core.util.CryptoUtil;
import io.graphenee.security.api.GxPasswordPolicyDataService;

@Service
@ConditionalOnProperty(prefix = "graphenee", name = "modules.enabled", havingValue = "true")
@Transactional
public class GxPasswordPolicyDataServiceImpl implements GxPasswordPolicyDataService {

	@Autowired
	GxPasswordPolicyRepository gxPasswordPolicyRepo;
	@Autowired
	GxNamespaceRepository gxNamespaceRepo;
	@Autowired
	GxUserAccountRepository userAccountRepo;
	@Autowired
	GxPasswordHistoryRepository passwordHistoryRepo;
	@Autowired
	GxDataService gxDataService;
	Pattern pattern;
	Matcher matcher;

	private Boolean findMinLengthExist(String password, int min) {
		return password.length() >= min ? true : false;
	}

	private Boolean findMaxUsernameExist(String username, String password, int max) {
		pattern = Pattern.compile("[^.]{" + ++max + "}");
		matcher = pattern.matcher(password);
		int i = 0;
		while (matcher.find(i++)) {
			if (username.contains(matcher.group()))
				return false;
		}
		return true;
	}

	private Boolean findMinUpperCaseCharExist(String password, int min) {
		if (min == 0)
			return true;
		pattern = Pattern.compile("[A-Z]");
		matcher = pattern.matcher(password);
		while (matcher.find()) {
			min--;
			if (min == 0)
				return true;
		}
		return false;
	}

	private Boolean findMinLowerCaseCharExist(String password, int min) {
		if (min == 0)
			return true;
		pattern = Pattern.compile("[a-z]");
		matcher = pattern.matcher(password);
		while (matcher.find()) {
			min--;
			if (min == 0)
				return true;
		}
		return false;
	}

	private Boolean findMinNumbersExist(String password, int min) {
		if (min == 0)
			return true;
		pattern = Pattern.compile("[\\d]");
		matcher = pattern.matcher(password);
		while (matcher.find()) {
			min--;
			if (min == 0)
				return true;
		}
		return false;
	}

	private Boolean findMinSpecialCharExist(String password, int min) {
		if (min == 0)
			return true;
		pattern = Pattern.compile("[!$#&^|~?%]");
		matcher = pattern.matcher(password);
		while (matcher.find()) {
			min--;
			if (min == 0)
				return true;
		}
		return false;
	}

	@Override
	public Boolean findPasswordIsValid(String namespace, String username, String password) {
		GxPasswordPolicy entity = gxPasswordPolicyRepo.findOneByGxNamespaceNamespaceAndIsActiveTrue(namespace);

		if (findMinLengthExist(password, entity.getMinLength())
				&& (!entity.getIsUserUsernameAllowed() || findMaxUsernameExist(username, password, entity.getMaxAllowedMatchingUserName()))
				&& findMinUpperCaseCharExist(password, entity.getMinUppercase()) && findMinLowerCaseCharExist(password, entity.getMinLowercase())
				&& findMinNumbersExist(password, entity.getMinNumbers()) && findMinSpecialCharExist(password, entity.getMinSpecialCharacters()))
			return true;
		return false;
	}

	@Override
	public void assertPasswordPolicy(GxPasswordPolicyBean entity, String username, String password) throws AssertionError {
		if (!findMinLengthExist(password, entity.getMinLength()))
			throw new AssertionError("Password must be minimum of " + entity.getMinLength() + " characters.");
		if (entity.getIsUserUsernameAllowed() && !findMaxUsernameExist(username, password, entity.getMaxAllowedMatchingUserName()))
			throw new AssertionError("Password must not contain " + entity.getMaxAllowedMatchingUserName() + " or more consecutive characters from username.");
		if (!findMinUpperCaseCharExist(password, entity.getMinUppercase()))
			throw new AssertionError("Password must contain at least " + entity.getMinUppercase() + " upper case letter(s).");
		if (!findMinLowerCaseCharExist(password, entity.getMinLowercase()))
			throw new AssertionError("Password must contain at least " + entity.getMinLowercase() + " lower case letter(s).");
		if (!findMinNumbersExist(password, entity.getMinNumbers()))
			throw new AssertionError("Password must contain at least " + entity.getMinNumbers() + " digit(s).");
		if (!findMinSpecialCharExist(password, entity.getMinSpecialCharacters()))
			throw new AssertionError("Password must contain at least " + entity.getMinUppercase() + " special character(s).");
	}

	private GxNamespaceBean makeNamespaceBean(GxNamespace entity) {
		GxNamespaceBean bean = new GxNamespaceBean();
		bean.setOid(entity.getOid());
		bean.setNamespace(entity.getNamespace());
		bean.setNamespaceDescription(entity.getNamespaceDescription());
		bean.setIsActive(entity.getIsActive());
		bean.setIsProtected(entity.getIsProtected());
		return bean;
	}

	private GxPasswordPolicyBean makePasswordPolicyBean(GxPasswordPolicy entity) {
		GxPasswordPolicyBean bean = new GxPasswordPolicyBean();
		bean.setOid(entity.getOid());
		bean.setMaxHistory(entity.getMaxHistory());
		bean.setMaxAge(entity.getMaxAge());
		bean.setMinLength(entity.getMinLength());
		bean.setIsUserUsernameAllowed(entity.getIsUserUsernameAllowed());
		bean.setMaxAllowedMatchingUserName(entity.getMaxAllowedMatchingUserName());
		bean.setMinUppercase(entity.getMinUppercase());
		bean.setMinLowercase(entity.getMinLowercase());
		bean.setMinNumbers(entity.getMinNumbers());
		bean.setMinSpecialCharacters(entity.getMinSpecialCharacters());
		bean.setIsActive(entity.getIsActive());
		bean.setPasswordPolicyName(entity.getPasswordPolicyName());
		bean.setGxNamespaceBeanFault(BeanFault.beanFault(entity.getGxNamespace().getOid(), (oid) -> {
			return makeNamespaceBean(gxNamespaceRepo.findOne(oid));
		}));
		return bean;
	}

	@Override
	public List<GxPasswordPolicyBean> findPasswordPolicyByNamespace(GxNamespaceBean gxNamespaceBean) {
		return gxPasswordPolicyRepo.findAllByGxNamespaceNamespace(gxNamespaceBean.getNamespace()).stream().map(this::makePasswordPolicyBean).collect(Collectors.toList());
	}

	@Override
	public GxPasswordPolicyBean findOnePasswordPolicyByNamespace(GxNamespaceBean gxNamespaceBean) {
		GxPasswordPolicy entity = gxPasswordPolicyRepo.findOneByGxNamespaceNamespace(gxNamespaceBean.getNamespace());
		if (entity == null)
			return null;
		return makePasswordPolicyBean(entity);
	}

	@Override
	public GxPasswordPolicyBean createOrUpdate(GxPasswordPolicyBean bean) {
		GxPasswordPolicy entity;
		if (bean.getOid() == null)
			entity = new GxPasswordPolicy();
		else
			entity = gxPasswordPolicyRepo.findOne(bean.getOid());
		entity = gxPasswordPolicyRepo.save(toEntity(entity, bean));
		bean.setOid(entity.getOid());
		return bean;
	}

	private GxPasswordPolicy toEntity(GxPasswordPolicy entity, GxPasswordPolicyBean bean) {
		entity.setMaxHistory(bean.getMaxHistory());
		entity.setMaxAge(bean.getMaxAge());
		entity.setMinLength(bean.getMinLength());
		entity.setIsUserUsernameAllowed(bean.getIsUserUsernameAllowed());
		entity.setMaxAllowedMatchingUserName(bean.getMaxAllowedMatchingUserName());
		entity.setMinUppercase(bean.getMinUppercase());
		entity.setMinLowercase(bean.getMinLowercase());
		entity.setMinNumbers(bean.getMinNumbers());
		entity.setMinSpecialCharacters(bean.getMinSpecialCharacters());
		entity.setIsActive(bean.getIsActive());
		entity.setPasswordPolicyName(bean.getPasswordPolicyName());
		if (bean.getGxNamespaceBeanFault() != null)
			entity.setGxNamespace(gxNamespaceRepo.findOne(bean.getGxNamespaceBeanFault().getOid()));
		return entity;
	}

	@Override
	public void delete(GxPasswordPolicyBean bean) {
		gxPasswordPolicyRepo.delete(bean.getOid());
	}

	@Override
	public void changePasswordPolicyApply(String username, String oldPassword, String newPassword, String confirmPassword) throws ChangePasswordFailedException {
		// fields validation apply
		GxUserAccountBean userAccountBean = gxDataService.findUserAccountByUsernameAndPassword(username, oldPassword);
		if (userAccountBean == null)
			throw new ChangePasswordFailedException("Current password did not match.");
		if (!newPassword.equals(confirmPassword))
			throw new ChangePasswordFailedException("New password did not match with confirm password");

		// password policy apply
		GxNamespaceBean namespaceBean = gxDataService.findNamespace("org.qatarbiobank.qbbrp");
		GxPasswordPolicyBean passwordPolicyBean = findOnePasswordPolicyByNamespace(namespaceBean);
		if (passwordPolicyBean == null) {
			passwordPolicyBean = new GxPasswordPolicyBean();
			passwordPolicyBean.setPasswordPolicyName(namespaceBean.getNamespace() + " Policy");
			passwordPolicyBean.setGxNamespaceBeanFault(BeanFault.beanFault(namespaceBean.getOid(), namespaceBean));
		}
		try {
			assertPasswordPolicy(passwordPolicyBean, username, newPassword);
		} catch (AssertionError e) {
			throw new ChangePasswordFailedException(e.getMessage());
		}

		Integer maxHistory = passwordPolicyBean.getMaxHistory();
		String encryptedPassword = CryptoUtil.createPasswordHash(newPassword);
		GxUserAccount userAccount = userAccountRepo.findByUsername(username);
		if (maxHistory > 0) {
			// is password match with current password
			if (userAccount.getPassword().equals(encryptedPassword)) {
				throw new ChangePasswordFailedException("password already exists.");
			}
			if (maxHistory > 1) {
				// is password match with histories passwords
				List<GxPasswordHistory> histories = passwordHistoryRepo.findAllByGxUserAccountOidOrderByPasswordDateDesc(userAccountBean.getOid());
				for (GxPasswordHistory history : histories) {
					if (history.getHashedPassword().equals(encryptedPassword))
						throw new ChangePasswordFailedException("password already exists.");
				}
				// password histories update
				if (histories.size() > 0 && histories.size() == maxHistory - 1)
					passwordHistoryRepo.delete(histories.get(histories.size() - 1));
				GxPasswordHistory passwordHistory = new GxPasswordHistory();
				passwordHistory.setGxUserAccount(userAccount);
				passwordHistory.setHashedPassword(userAccount.getPassword());
				passwordHistory.setPasswordDate(new Timestamp(System.currentTimeMillis()));
				passwordHistoryRepo.save(passwordHistory);
			}
		}
		userAccount.setIsPasswordChangeRequired(false);
		userAccount.setPassword(encryptedPassword);
		userAccountRepo.save(userAccount);
	}

	@Override
	public Boolean isPasswordExpired(GxUserAccountBean userAccountBean) {
		GxPasswordHistory passwordHistory = passwordHistoryRepo.findTop1ByGxUserAccountOidOrderByPasswordDateDesc(userAccountBean.getOid());
		Long changedDate = userAccountRepo.findOne(userAccountBean.getOid()).getAccountActivationDate().getTime();
		if (passwordHistory != null)
			changedDate = passwordHistory.getPasswordDate().getTime();
		if (changedDate == null)
			return false;
		Long currDate = new Timestamp(System.currentTimeMillis()).getTime();
		Long diff = currDate - changedDate;
		Integer days = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		GxNamespaceBean namespaceBean = gxDataService.findNamespace("org.qatarbiobank.qbbrp");
		GxPasswordPolicyBean passwordPolicyBean = findOnePasswordPolicyByNamespace(namespaceBean);
		if (passwordPolicyBean == null) {
			passwordPolicyBean = new GxPasswordPolicyBean();
			passwordPolicyBean.setPasswordPolicyName(namespaceBean.getNamespace() + " Policy");
			passwordPolicyBean.setGxNamespaceBeanFault(BeanFault.beanFault(namespaceBean.getOid(), namespaceBean));
		}
		return days > passwordPolicyBean.getMaxAge();
	}

}