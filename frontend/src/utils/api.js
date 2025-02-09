import axios from "axios";

export const loginUser = async (form) => {
  const response = await axios.post("http://localhost:8080/api/auth/login", form);
  return response.data;
};

export const registerUser = async (form) => {
  await axios.post("http://localhost:8080/api/users", form);
};
