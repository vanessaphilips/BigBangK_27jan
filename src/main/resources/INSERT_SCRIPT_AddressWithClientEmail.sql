CREATE VIEW addresswithclientemail
AS 
SELECT
	email,
	street,
    number,
    postalcode,
    city,
    country
FROM
	address
INNER JOIN
	client USING (postalcode, number)
GROUP BY
	client.email;

    
