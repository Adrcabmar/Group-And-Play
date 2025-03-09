import axios from "axios";

export const loginUser = async (form) => {
  const response = await axios.post("http://localhost:8080/api/auth/login", form);
  return response.data;
};

export const registerUser = async (form) => {
  await axios.post("http://localhost:8080/api/users", form);
};

export const getCurrentUser = async ({token}) => {
  const response = await fetch("http://localhost:8080/api/users/auth/current-user", {
    method: "GET",
    headers: {
    "Authorization": `Bearer ${token}`,  // Incluir el token JWT en el encabezado
    "Content-Type": "application/json",
    },
    credentials: "include", // Si usas cookies, de lo contrario puedes eliminar esto
  });

    const data = await response.json();

    return data;
}