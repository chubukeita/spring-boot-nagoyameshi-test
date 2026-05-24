document.addEventListener("DOMContentLoaded", () => {
  const emailInput = document.getElementById("email");
  const submitButton = document.getElementById("rejoinButton");
  const rejoinForm = submitButton?.closest("form");
  if (!emailInput || !submitButton || !rejoinForm) return;

  let isSubmitting = false;

  let errorMessage = document.getElementById("emailError");
  if (!errorMessage) {
    errorMessage = document.createElement("div");
    errorMessage.id = "emailError";
    errorMessage.className = "text-danger small mt-1";
    emailInput.closest(".form-group")?.appendChild(errorMessage);
  }

  const validateEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

  const validate = () => {
    if (isSubmitting) return;

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

  rejoinForm.addEventListener("submit", (event) => {
    // Prevent duplicate submission requests caused by rapid repeated clicks.
    if (isSubmitting) {
      event.preventDefault();
      return;
    }

    validate();
    if (submitButton.disabled) {
      event.preventDefault();
      return;
    }

    isSubmitting = true;
    submitButton.disabled = true;
    submitButton.textContent = "送信中...";
  });

  validate();
});
