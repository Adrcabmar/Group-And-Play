import { useEffect, useState } from "react";
import { getCurrentUser } from "../utils/api";
import { useUser } from "../components/UserContext";

export const useCurrentUser = () => {
  const { user, setUser } = useUser();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("jwt");

    if (user) {
      setLoading(false);
    } else if (token) {
      const fetchCurrentUser = async () => {
        try {
          const data = await getCurrentUser({ token });
          setUser(data.user);
          localStorage.setItem("user", JSON.stringify(data.user));
        } catch (error) {
          console.error("Error fetching current user:", error);
          setUser(null);
          localStorage.removeItem("user");
        } finally {
          setLoading(false);
        }
      };

      fetchCurrentUser();
    } else {
      setLoading(false);
    }
  }, [user, setUser]);

  return { currentUser: user, loading, setCurrentUser: setUser };
};
