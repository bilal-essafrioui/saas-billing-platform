export function isEmpty(value: string): boolean {
  return value.trim().length === 0;
}

export function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

export function isValidName(name: string): boolean {
  const value = name.trim();
  return value.length >= 2 && value.length <= 50;
}

export function isValidPassword(password: string): boolean {
  return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&.#_-])[A-Za-z\d@$!%*?&.#_-]{8,100}$/.test(
    password
  );
}

export function passwordsMatch(
  password: string,
  confirmPassword: string
): boolean {
  return password === confirmPassword;
}

// Validation for the registration form
export function validateRegisterForm(data: {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
}): string | null {

  if (isEmpty(data.firstName))
    return "First name is required.";

  if (!isValidName(data.firstName))
    return "First name must be between 2 and 50 characters.";

  if (isEmpty(data.lastName))
    return "Last name is required.";

  if (!isValidName(data.lastName))
    return "Last name must be between 2 and 50 characters.";

  if (isEmpty(data.email))
    return "Email is required.";

  if (!isValidEmail(data.email))
    return "Invalid email format.";

  if (isEmpty(data.password))
    return "Password is required.";

  if (!isValidPassword(data.password))
    return "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character.";

  if (!passwordsMatch(data.password, data.confirmPassword))
    return "Passwords do not match.";

  return null;
}

// validate email otp
export function isValidOtp(otp: string): boolean {
  return /^\d{6}$/.test(otp);
}

export function validateVerifyEmailForm(otp: string): string | null {
  if (!isValidOtp(otp)) {
    return "Verification code must contain exactly 6 digits.";
  }

  return null;
}