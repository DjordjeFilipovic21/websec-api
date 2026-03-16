# Kako pokrenuti projekat

1. Udji u `websec-api` folder.
2. Pokreni MySQL kontejner: `docker start websec-mysql56`
3. Proveri da je kontejner stvarno upaljen: `docker ps`
4. Pokreni backend: `./mvnw spring-boot:run`
5. U novom terminalu udji u `websec-ui` folder.
6. Pokreni UI: `python3 -m http.server 3000`
7. Otvori u browseru: `http://localhost:3000/login.html`

## Sta je implementirano

- Sredjen je **IDOR problem** za review-e.
- To znaci da korisnik vise ne moze da otvori ili menja review koji nije njegov, cak i ako menja URL ili salje rucno request.
- Ako neko pokusa da pristupi tudjem resursu, sada dobija `403` ili `404`, umesto da prodje.

- Dodata je zastita od **dictionary/brute-force login napada**.
- Posle 5 pogresnih pokusaja login-a, nalog se zakljuca na 2 minuta.
- U tom periodu login vraca `429 Too Many Requests`.
- Kad vreme prodje, moze ponovo da se pokusa login.

- UI je malo promenjen da prati ovo:
- Na login strani sada se razlikuje obican los password (`401`) i lock (`429`), pa korisnik dobije jasniju poruku.
- Review strana sada koristi user iz tokena (JWT), a ne veruje `userId` iz URL-a.
- Ako korisnik pokusa da otvori tudji review, dobice poruku i vraca ga nazad.
