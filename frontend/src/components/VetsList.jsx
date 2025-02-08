import { useEffect, useState } from "react";

function VetsList() {
    const [vets, setVets] = useState([]);

    useEffect(() => {
        fetch("http://localhost:8080/api/vets")
            .then(response => response.json())
            .then(data => setVets(data))
            .catch(error => console.error("Error fetching vets:", error));
    }, []);

    return (
        <div>
            <h1>List of Vets</h1>
            <ul>
                {vets.map(vet => (
                    <li key={vet.id}>{vet.firstName} {vet.lastName}</li>
                ))}
            </ul>
        </div>
    );
}

export default VetsList;
