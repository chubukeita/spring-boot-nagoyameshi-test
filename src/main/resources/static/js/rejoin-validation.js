document.addEventListener("DOMContentLoaded", () => {
  const emailInput = document.getElementById("email");
  const submitButton = document.getElementById("rejoinButton");
  if (!emailInput || !submitButton) return;

  let errorMessage = document.getElementById("emailError");
  if (!errorMessage) {
    errorMessage = document.createElement("div");
    errorMessage.id = "emailError";
    errorMessage.className = "text-danger small mt-1";
    emailInput.closest(".form-group")?.appendChild(errorMessage);
  }

  const validateEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

  const validate = () => {
    const value = (emailInput.value || "").trim();
    if (value.length === 0) {
      errorMessage.textContent = "メールアドレスを入力してください。";
      submitButton.disabled = true;
      return;
    }
    if (!validateEmail(value)) {
      errorMessage.textContent = "メールアドレスの形式が正しくありません。";
      submitButton.disabled = true;
      return;
    }
    errorMessage.textContent = "";
    submitButton.disabled = false;
  };

  emailInput.addEventListener("input", validate);
  validate();
});