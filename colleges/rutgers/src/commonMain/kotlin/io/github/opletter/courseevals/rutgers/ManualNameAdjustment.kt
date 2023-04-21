package io.github.opletter.courseevals.rutgers

// for typos, bad/inconsistent formatting, and more!
fun manualNameAdjustment(prof: String, code: String): String {
    return when (code.split(":").take(2).joinToString(":")) {
        "01:013" -> when (prof) {
            "HABBAL, MANAR" -> "HABBAL, MANAL" // 01:070, 01:074
            "KHAYYAT, EFE", "E, EFE", "EFE, E" -> "KHAYYAT, EMRAH" // 01:090, 01:195
            "YKHAYYAT" -> "KHAYYAT, YASMINE"
            else -> prof
        }

        "01:014" -> when (prof) {
            "AMARAL, MAMARAL" -> "AMARAL, MELISSA" // probably
            "CADENAJ, JESENIA" -> "CADENA, JESENIA"
            "GRIMMET, MURIEL" -> "GRIMMETT, MURIEL"
            "JACKSONBREWER, KARLA" -> "JACKSON-BREWER, KARLA"
            "PRICE, MELANYE" -> "PRICE, MELANIE"
            "RAMACHANDRANA, ANITHA", "RAMACHANDRAN, KAVITHA" -> "RAMACHANDRAN, ANITHA" // 2nd probably a typo
            "WALTON, JOHNSON" -> "JOHNSON, WALTON"
            "WHITNEYIII", "WHITNEYIII, JAMES", "WHITNEY, JANES", "WHITNEY, JAMES" -> "WHITNEY III, JAMES"
            else -> prof
        }

        "01:050" -> when (prof) {
            "ALONSO, BEJARANO" -> "ALONSO, CAROLINA" // 01:595
            "LINDHREEVES, ELIZABETH" -> "REEVES, ELIZABETH"
            "MARTINEZ-SAN, MIGUEL", "MARTINEZ, E" -> "MARTINEZ-SAN, YOLANDA" // 2nd probably / 01:195, 01:595
            "SIFUENTES, BEN" -> "SIFUENTES-JAUREGUI, BEN" // 01:090
            else -> prof
        }

        "01:070" -> when (prof) {
            "CABANES, CRUELLES" -> "CABANES, DAN"
            "GHASSEM-FACHAN", "GHASSEM-FACHAN, PARVIS" -> "GHASSEM-FACHANDI, PARVIS"
            "SCOTT, ROBB" -> "SCOTT, ROBERT"
            "WOODHOUSEBEYER, KATHARINE", "WOODHOUSE-BEYE" -> "WOODHOUSE-BEYER, KATHARINE" // 01:082
            else -> prof
        }

        "01:074" -> when (prof) {
            "HABBAL, MANAR" -> "HABBAL, MANAL" // 01:013
            else -> prof
        }

        "01:078" -> when (prof) {
            "VASILIAN, ASBED" -> "VASSILIAN, ASBED"
            else -> prof
        }

        "01:082" -> when (prof) {
            "MIEKE, PAULSEN" -> "PAULSEN, MIEKE"
            "PORTSNER, LAURIE" -> "PORSTNER, LAURIE"
            "WOODHOUSE-BEYER, KATHERINE", "WOODHOUSEBEYER, KATHERINE", "WOODHOUSEBEYER, KATHARI" -> "WOODHOUSE-BEYER, KATHARINE" // 01:070
            else -> prof
        }

        "01:090" -> when (prof) {
            "ALEXANDERFLOYD, NIKOL", "ALEXANDER, FLOYD", "ALEXANDER, NIKOL" -> "ALEXANDER-FLOYD, NIKOL" // 01:988
            "ANDROULAKIS, YANNIS", "ANDROULAKIS, IONNIS" -> "ANDROULAKIS, IOANNIS"
            "BAIER, BECCA" -> "BAIER, REBECCA"
            "BATHORY, D" -> "BATHORY, PETER" // probably
            "BEAL, MIKE", "BEALS, MIKE", "BEALS, MICHAEL" -> "BEALS, ROBERT" // probably
            "BERKELEY, SHORNA" -> "BERKELEY, SHORNNA"
            "BERKELY, S" -> "BERKELEY, SHORNNA"
            "BORRMAN-BEGG, CAROL" -> "BEGG, CAROL"
            "BRENNAN, C" -> "BRENNAN, T"  // probably
            "CAHILL, C" -> "CAHILL, KATE" // probably
            "DALBELLO, MARISA" -> "DALBELLO, MARIJA"
            "DEBORAH, AKS" -> "AKS, DEBORAH"
            "EFE, E" -> "EFE, KHAYYAT"  // 01:013, 01:195
            "GADSDEN, PETAL" -> "BRITTON-GADSDEN, PETAL"
            "GHASSEM-FACHAN" -> "GHASSEM-FACHANDI, PARVIS"
            "GONZALEZ, BARBARA", "GONZALEZ, PALMER" -> "GONZALEZ-PALMER, BARBARA"
            "GORDON, RAHJUAN" -> "GORDON, RAHJAUN"
            "HAGHANI, FAKHROLMOL" -> "HAGHANI, FAKHRI" // probably / 01:685, 01:988
            "HEUMANN, MILTOW" -> "HEUMANN, MILTON"
            "KAMINISKI, LAURA" -> "KAMINSKI, LAURA"
            "KAPTAN, ALLEN" -> "KAPTAN, SENEM"
            "KAUFMAN, TRICIA" -> "KAUFMAN, TRISHA"
            "KOSINKI, KIMBERLY" -> "KOSINSKI, KIMBERLY"
            "KULIKOWSKI, CHARLES" -> "KULIKOWSKI, CASIMIR" // probably
            "LARANJEIRO, JOSE" -> "LARANJEIRO, JOE"
            "LENAHAN, CLEARY" -> "LENAHAN, JENNIFER"
            "LEWIS, FRANKIE" -> "LEWIS, FRANCIS" // probably
            "LOPEZ, KATHY" -> "LOPEZ, KATHERINE" // probably / 01:508
            "LORD, N" -> "LORD, MUFFIN" // probably
            "MANDELBAUM, JENNY" -> "MANDELBAUM, JENNIFER"
            "MARKOWITZ, NORMAM" -> "MARKOWITZ, NORMAN"
            "MARTINEZSANMIGUEL, YOLANDA", "MARTINEZSAN, YOLANDA" -> "MIGUEL, YOLANDA" // shortenedd the name
            "MCCROSSIN, TRIP", "MCCROSSIN, T", "MCCROSSIN, EDWAD" -> "MCCROSSIN, EDWARD" // 01:730
            "MCGREWC, CHARLES" -> "MCGREW, CHARLES"
            "MONTELIONE, GUY" -> "MONTELIONE, GAETANO"
            "NAZARIO, JUILO", "NAZARI, JULIO", "NAZARIO, I" -> "NAZARIO, JULIO"
            "OTEROTORRES, DAMARIUS" -> "OTEROTORRES, DAMARIS"
            "RAMIRIZ, CATHERINE", "RAMERIZ, CATHERINE" -> "RAMIREZ, CATHERINE" // probably
            "REARDON, ABBIE" -> "REARDON, ABIGAIL"
            "RITTER, JUCIA", "JULIA, RITTER" -> "RITTER, JULIA"
            "RUSSELLJONES, SANDRA" -> "RUSSELL-JONES, SANDRA"
            "SALZMAN, HAROLD" -> "SALZMAN, HAL" // I think?
            "SCHNETZER, STEVE" -> "SCHNETZER, STEPHEN"
            "SCOTT, KATHY" -> "SCOTT, KATHLEEN"
            "SIFUENTESJAUREGU, BEN", "SIFUENTES, BEN" -> "SIFUENTES-JAUREGU, BEN" // 01:050
            "SIMMONS, RV" -> "SIMMONS, RICHARD" // probably
            "SMITH, DR" -> "SMITH, RANDALL" // probably
            "SPEAR, BILL" -> "SPEAR, WILLIAM" // probably
            "SPERLING, ALI" -> "SPERLING, ALESSANDRA"
            "ST, GEORGE" -> "ST. GEORGE, MICHELLE"
            "SUTTON, TOPHER" -> "SUTTON, CHRISTOPHER"
            "WENZEL, JACK" -> "WENZEL, JOHN"
            "WILSON, JENNIFER" -> "GIBSON, JENNIFER"
            "WUPALUMBO, CONNIELAUR" -> "WU, CONNIE" // other is PALUMBO, LAURA
            else -> prof
        }

        "01:119" -> when (prof) {
            "GLODOWSKI, TROTTA" -> "GLODOWSKI, DOREEN" // 01:447
            "WILLIARD, ALEXANDRA" -> "WILLIAMS, ALEXANDRA"
            else -> prof
        }

        "01:146" -> when (prof) {
            "CARR-SCHMID, A" -> "CARR-SCHMID, ELEANOR"
            "FIRESTEIN-MILLER, BONNIE" -> "FIRESTEIN, BONNIE"
            "GOLFETI, ROSELI" -> "GOLFETTI, ROSELI"
            "WU, LONGSUN", "LONG, JUNWU" -> "WU, LONG-JUN"
            else -> prof
        }

        "01:160" -> when (prof) {
            "ALTINIS, CHRISTINE" -> "ALTINIS-KIRAZ, CHRISTINE"
            "ARNOLD, EDDY" -> "ARNOLD, EDWARD"
            "ASHWINI, RANADE" -> "RANADE, ASHWINI"
            "BILLMERS, BOB", "BILLMERS, BILLMERS" -> "BILLMERS, ROBERT"
            "BOIKESS, BOB" -> "BOIKESS, ROBERT"
            "CHEN, KY" -> "CHEN, KUANG-YU"
            "DISMUKES, GERARD" -> "DISMUKES, CHARLES" // probably
            "HINCH, BARBARA" -> "HINCH, JANE"
            "HYUNJIN, KIM" -> "KIM, HYUNJIN"
            "JIMINEZ, LESLIE" -> "JIMENEZ, LESLIE"
            "KROGH-JESPERSE, KARSTEN" -> "KROGH-JESPERSEN, KARSTEN"
            "MARCOTRIGIANO, JOESEPH" -> "MARCOTRIGIANO, JOSEPH"
            "MARVASTI, SATAREH" -> "MARVASTI, SETAREH"
            "OLSON, WILIMA" -> "OLSON, WILMA"
            "PRAMINIK, SANHITA" -> "PRAMANIK, SANHITA"
            "RABEONY, MANSES" -> "RABEONY, MANESE"
            "ROMSTED, LAWRENCE" -> "ROMSTED, LAURENCE"
            "ROYCHOWDUHURY, LIPIKA", "ROYCHOWDURY, LIPIKA" -> "ROYCHOWDHURY, LIPIKA"
            "SHANKAR, NIRMILA" -> "SHANKAR, NIRMALA"
            "SOUNDARAJAN, NACHIMUTHU" -> "SOUNDARARAJAN, NACHIMUTHU"
            "WOOSEOK, KI" -> "KI, WOOSEOK"
            "YORK, DARREN" -> "YORK, DARRIN"
            else -> prof
        }

        "01:175" -> when (prof) {
            "FLITTERMANLEWIS, SANDY" -> "FLITTERMAN-LEWIS, SANDRA" // 01:354
            "NIGRIN, ACBERT" -> "NIGRIN, ALBERT"
            else -> prof
        }

        "01:185" -> when (prof) {
            "LUTKEN, CAROLYN" -> "LUTKEN, JANE"
            else -> prof
        }

        "01:190" -> when (prof) {
            "ALLENHORNBLOWE, EMILY" -> "ALLEN-HORNBLOWER, EMILY"
            "FISHER, JAY" -> "FISHER, JOHN" // 01:490, 01:580, 01:615 / confident about this
            else -> prof
        }

        "01:195" -> when (prof) {
            "FANELLI, LAUREN" -> "FANELLI-TEAGUE, LAUREN" // 01:355
            "KHYYAT, EMRAH", "KHAYYAT, EFE", "EFE, E" -> "KHAYYAT, EMRAH" // 01:013, 01:090
            "KITZINGER, CHLOE", "KITZINGER, C" -> "KITZINGER-SHEDLOCK, CHLOE"
            "LIDIA, LEVKOVITCH" -> "LEVKOVITCH, LIDIA"
            "MARTINEZ-SAN, MIGUEL", "MARTINEZ, E" -> "MARTINEZ-SAN, YOLANDA" // 2nd probably / 01:050, 01:595
            "SCLAFANI, KATHY", "KATHLEEN, SCLAFANI" -> "SCLAFANI, KATHLEEN"
            "STEVEN, GONZAGOWSKI" -> "GONZAGOWSKI, STEVEN"
            "YU-I, HSIEH" -> "HSIEH, YU-I"
            else -> prof
        }

        "01:198" -> when (prof) {
            "CHIRCO, JT", "CHRICO, J" -> "CHIRCO, JOHN"
            "DESSAI, ANAGHA" -> "DESAI, ANAGHA" // probably
            "GUNAWARDENA, ANDY" -> "GUNAWARDENA, ANANDA"
            "LMIELINSKI, T" -> "IMIELINSKI, TOMASZ" // 01:960
            "MIRANDA, GARCIA" -> "MIRANDA, ANTONIO"
            "NARAYANA, GANAPATHY" -> "NARAYANA, SRINIVAS"
            "RAMAKRISHNA, R" -> "RAMAKRISHNAN, REMYA" // probably
            "SHETTY, KARTIK" -> "SHETTY, KARTHIK"
            "SRIVASTAVA, PRAHKAR" -> "SRIVASTAVA, PRAKHAR"
            "WEIDENHOFT, J" -> "WIEDENHOEFT, JOHN"
            "ZHIQIANG, TANG" -> "TANG, ZHIQIANG"
            else -> prof
        }

        "01:202" -> when (prof) {
            "KOHL, JAY" -> "KOHL, JAMES" // I think
            else -> prof
        }

        "01:220" -> when (prof) {
            "BHUYAN, SUE" -> "BHUYAN, SUSAN"
            "CARBONELL-N, O", "CARBONELL, ORIOL" -> "CARBONELL-NICOLAU, ORIOL"
            "CROCKETT, BARBARA" -> "CROCKETT, ERIN"
            "NOSRATABADI, S" -> "NOSRATABADI, HASSAN"
            "SJOSTROM, TOMAS", "SJOSTROM, T" -> "SJOSTROM, JOHN"
            "TORRES-RENYA, OSCAR" -> "TORRES-REYNA, OSCAR"
            else -> prof
        }

        "01:351" -> when (prof) {
            "WIRSTIUK, LYRYSSA" -> "WIRSTIUK, LARYSSA"
            "RZIGALINKSKI, CHRISTOPHER" -> "RZIGALINSKI, CHRISTOPHER"
            "HOBOYAN, LESLIEANN" -> "HOBAYAN, LESLIEANN"
            "PEARLSTEIN, RANDY" -> "PEARLSTEIN, RANDALL"
            "KLAVER, BECCA" -> "KLAVER, REBECCA"
            "NIKOLOPOULOS, EVAGELOS" -> "NIKOLOPOULOS, ANGELO"
            "OSBORNE, SUSAN" -> "OSBORN, SUSAN"
            "WALLISHUGHES, EMILY" -> "HUGHES, EMILY"
            else -> prof
        }

        "01:354" -> when (prof) {
            "SAVERINO, ANASTASIA" -> "SAVERINO, ANASTASAA"
            "FLITTERMANLEW, SANDY", "FLITTERMANLEWI, SANDY", "FLITTERMANLEWIS, SANDY" -> "FLITTERMAN-LEWIS, SANDRA"
            else -> prof
        }

        "01:355" -> when (prof) {
            "BASS, JQNATHAN" -> "BASS, JONATHAN"
            "BORIEHOLTZ, DEBBIE" -> "BORIEHOLTZ, DEBRA"
            "BOUTIN, CLAUDE", "BOUTIN, HARRYCLAUDE" -> "BOUTIN, HARRY"
            "BRIE, ASHLEY" -> "ASHLEY, BRIE"
            "CANTOR, NICOLE" -> "COHEN, NICOLE" // probably typo
            "CHOWDHURY, NANDINCH" -> "CHOWDHURY, NANDINI"
            "COHAN, DARCY" -> "GIOIA, DARCY" // probably typo
            "CONIERCOLLIER, SUSAN" -> "COINERCOLLIER, SUSAN"
            "DAZA, VANESSA" -> "DAZA-HECK, VANESSA"
            "DUFFY, MIKE" -> "DUFFY, MICHAEL"
            "HARUKI, EDA" -> "EDA, HARUKI"
            "FANELLI, LAUREN" -> "FANELLI-TEAGUE, LAUREN" // 01:195
            "FOLEM, SEAN" -> "FOLEY, SEAN"
            "GILMARTIN, VGILMAR" -> "GILMARTIN, VIRGINIA"
            "GOELLER, ANGIESZKA" -> "GOELLER, AGNIESZKA"
            "GOODYEAR, CHRISTINE" -> "GOODYEAR, TINA"
            "HAMLET, BMENDA" -> "HAMLET, BRENDA"
            "JAVONSKI, JORDANCO" -> "JOVANOSKI, JORDANCO"
            "LIBBY, PHILIP", "LIBBY, PHILLIP", "LIBBY, PHILIPANDR", "LIBBY, PHILIPANDREW" -> "LIBBY, ANDREW"
            "NACHESCU, VOLCHITA" -> "NACHESCU, VOICHITA"
            "NELSON, BOB" -> "NELSON, ROBERT"
            "NEVINS, JESSICA" -> "NEVIN, JESSICA"
            "OLTARZEWSKI, MJ", "OLTARZEWSKI, A" -> "OLTARZEWSKI, MARYJANE" // 2nd probably
            "PERSSON, TORLIEF", "PERSSON, TORLEIF" -> "PERSSON, BO"
            "ROBINSON, MIXON" -> "ROBINSON, RALEIGH"
            "SANDBERG, GOLDEN", "SANDBERG, LENA", "GOLDENSANDBERG, LENA" -> "SANDBERG-GOLDEN, LENA"
            "SAVILLE, DUDLEYALEX", "SAVILLE, ALEX" -> "SAVILLE, DUDLEY"
            "SCHMID, LETITIA" -> "SCHMID, LETIZIA"
            "SCHROEPHER, GEORGE" -> "SCHROEPFER, GEORGE"
            "TUCKSON, NINA" -> "TUCKSON, NIA"
            "WILFORD, KATHY" -> "WILFORD, KATHLEEN"
            "ZIEBA, IZABELLA" -> "ZIEBA, IZABELA" // not sure which one is correct tbh
            else -> prof
        }

        "01:356" -> when (prof) {
            "CAPONEGRO, MJHAELA" -> "CAPONEGRO, MIHAELA"
            "CUSUMANO, RICH" -> "CUSUMANO, RICHARD"
            "CUSUMANO, ROSE" -> "CUSUMANO, ROSEMARIE" // would be auto-fixed if they didn't have the same last name!
            "ELKAHATEEB, HEBATALLA" -> "ELKHATEEB, HEBATALLA"
            "MARRONE, CAROLE" -> "MARRONE, CAROLINE"
            "MATHEWS, CHISTIAN" -> "MATHEWS, CHRISTIAN"
            "NAVARRO, NELA", "NAVARRO, NECA", "NAVARRON, NELA", "NAVARROLAPOINT, NELA" -> "NAVARRO-LAPOINTE, NELA"
            "NACHESCUV, VOICHITA" -> "NACHESCU, VOICHITA"
            "SCHNATTER, KERISTIN" -> "SCHNATTER, KERSTIN"
            "SLOVICK, SHARRON" -> "SLOVICK, SHARON"
            "TANNER, AARON" -> "TANNER, WILLIAM"
            else -> prof
        }

        "01:358" -> when (prof) {
            "BUCKLEY, MATHEW" -> "BUCKLEY, MATTHEW"
            "CAMRADA, JULIE", "CAMARADA, JULIE" -> "CAMARDA, JULIE" // this name isn't present but seems to be right
            "DIENSTR, RICHARD" -> "DIENST, RICHARD"
            "IBIRONKE, BODE" -> "IBIRONKE, OLABODE"
            "LAWRENCE, JEFFERY" -> "LAWRENCE, JEFFREY"
            "MANGHARAN, MUKTI" -> "MANGHARAM, MUKTI"
            "ROBOLIN, STEPHANIE" -> "ROBOLIN, STEPHANE"
            "SCANLON, LARRY" -> "SCANLON, LAWRENCE"
            "WALL, CWALL" -> "WALL, CHERYL" // probably
            else -> prof
        }

        "01:359" -> when (prof) {
            "EDIAMOND, ELIN" -> "DIAMOND, ELIN"
            "GROGAN, KRIRTIN" -> "GROGAN, KRISTIN"
            "GUZZARDOTAMARG, ISABEL", "GUZZARDOTAMARGO" -> "TAMARGO, ISABEL"
            "KITZINGER, CHLOE" -> "KITZINGER-SHEDLOCK, CHLOE"
            "IBIRONKE, BODE" -> "IBIRONKE, OLABODE" // 01:358
            "MATHES, CARTHER" -> "MATHES, CARTER"
            "PERSSON, TORLEIF" -> "PERSSON, BO" // 01:355
            else -> prof
        }

        "01:360" -> when (prof) {
            "KELEMEN, D" -> "KELEMEN, ROGER"
            "PETURRSON, SVANUR" -> "PETURSSON, SVANUR" // 01:790
            else -> prof
        }

        "01:377" -> when (prof) {
            "BRIDENBAUGH, E" -> "BRIDENBAUGH, JOHN"
            "COSLOY, JAIME", "COSLOY, J" -> "HECHT-COSLOY, JAIME" // 01:955
            "DANDREA, CHRISTOPHER", "CHRISTOPHER, DANDREA" -> "D'ANDREA, CHRISTOPHER"
            "GLADIS, KATHI" -> "GLADIS, KATHLEEN"
            "JOANNE, HUNT" -> "HUNT, JOANNE"
            "MYRON, FINKELSTEIN" -> "FINKELSTEIN, MYRON"
            "NANCY, GOLDBERG" -> "GOLDBERG, NANCY"
            "ROBELL, NAGLE" -> "ROBELL, NICOLE"
            else -> prof
        }

        "01:420" -> when (prof) {
            "BRANTON-DESRIS, JENNIFER" -> "BRANTON-DESRIS, JENIFER"
            "KARMARMAR", "KARMAKAR, MEDHA" -> "KARMARKAR, MEDHA"
            "PAIRET, VINAS" -> "PAIRET, ANA"
            else -> prof
        }

        "01:447" -> when (prof) {
            "GLODOWSKI, TROTTA" -> "GLODOWSKI, DOREEN" // 01:119
            else -> prof
        }

        "01:450" -> when (prof) {
            "GHERTNER, DAVID", "GHERTNER, D" -> "GHERTNER, ASHER"
            else -> prof
        }

        "01:460" -> when (prof) {
            "AUBRY, M-P" -> "AUBRY, MARIE-PIERRE"
            "DELANEY, JERRY" -> "DELANEY, JEREMY" // probably
            "HERZBERG, G" -> "HERZBERG, CLAUDE" // typo probably
            "MORTLOCK, RICK" -> "MORTLOCK, RICHARD"
            "PETER, SUGARMAN" -> "SUGARMAN, PETER"
            "SEVERMAN, SILKE" -> "SEVERMANN, SILKE"
            "SEQUEIRA, PERAZA" -> "SEQUEIRA, CESAR"
            "TIKOO, SONIA" -> "TIKOO-SCHANTZ, SONIA"
            "VANTOGEREN, JILL" -> "VANTONGEREN, JILL"
            else -> prof
        }

        "01:489" -> when (prof) {
            "KOURTIGAVALAS, KATHERINE" -> "KOURTI, KATHERINE"
            else -> prof
        }

        "01:490" -> when (prof) {
            "FISHER, JAY" -> "FISHER, JOHN" // 01:190, 01:580, 01:615
            else -> prof
        }

        "01:506" -> when (prof) {
            "BELLRODEN, RUDOLPHDON" -> "BELL, RUDY"
            "LAURIA-SANTIAG, ALDO" -> "LAURIA, ALDO"
            "LIVINGSTON, JIM", "LIVINGSTON, JA" -> "LIVINGSTON, JAMES"
            "O'BRASSILL-KUL, KRISTIN" -> "O'BRASSILL-KULFAN, KRISTIN"
            "WOODHOUSEBEYER, KATHERINE" -> "WOODHOUSE-BEYER, KATHARINE"
            else -> prof
        }

        "01:508" -> when (prof) {
            "LAURIA, SANTIAGO", "LAURIA, SANTIAG", "LAURIA, ALDO" -> "LAURIA-SANTIAGO, ALDO" // 01:512, 01:595
            "LOPEZ, KATHY" -> "LOPEZ, KATHLEEN" // probably
            "RATZMAN, ELI" -> "RATZMAN, ELLIOT" // 01:563, 01:685
            "RUSSELL-JONES, SANDY", "RUSSELL, SANDRA" -> "RUSSELL-JONES, SANDRA" // 01:090, 01:685
            else -> prof
        }

        "01:510" -> when (prof) {
            "DIBATTISTA, ANTONY" -> "DIBATTISTA, ANTHONY"
            "FIGUEIRA, TOM" -> "FIGUEIRA, THOMAS"
            "REINERT, STEVE" -> "REINERT, STEPHEN"
            else -> prof
        }

        "01:512" -> when (prof) {
            "GREENBERG DA" -> "GREENBERG, DAVID"
            "GREENBERG, DO" -> "GREENBERG, DOUG"
            "JEFFREY, TOM" -> "JEFFREY, THOMAS"
            "LAURIA, SANTIAGO", "LAURIA, ALDO" -> "LAURIA-SANTIAGO, ALDO" // 01:508, 01:595
            "LEE, KATHERINE", "LEE, KATIE" -> "LEE, KATHARINE"
            "MARKOWITZ, WORMAN" -> "MARKOWITZ, NORMAN" // 01:090
            "OBRASSILLKULF, KRISTIN" -> "O'BRASSILL-KULFAN, KRISTIN" // 01:506
            else -> prof
        }

        "01:556" -> when (prof) {
            "WHITNEY, JAMES", "WHITNEY, J" -> "WHITNEY III, JAMES"
            else -> prof
        }

        "01:560" -> when (prof) {
            "CARMELA, SCALA" -> "SCALA, CARMELA"
            "DONATA, PANIZZA" -> "PANIZZA, DONATA"
            "FOGNANI, ARIANA" -> "FOGNANI, ARIANNA"
            "HIROMI, KANEDA" -> "KANEDA, HIROMI"
            "ILONA, HRENKO" -> "HRENKO, ILONA"
            "TIMOTHY, CURCIO" -> "CURCIO, TIMOTHY"
            "TIZIANO, CHERUBINI" -> "CHERUBINI, TIZIANO"
            else -> prof
        }

        "01:563" -> when (prof) {
            "RATZMAN, ELI" -> "RATZMAN, ELLIOT" // 01:508, 01:685
            "SHANDLER, JEFFERY" -> "SHANDLER, JEFFREY"
            else -> prof
        }

        "01:565" -> when (prof) {
            "FLEMING, NATUSKO", "FLEMING, NATSUKO" -> "BUURSTRA, NATSUKO" // got married seemingly
            "OKADA-HAWALKA, YASUKO" -> "HAWALKA, YASUKO"
            "PIOTROWSKI, MIKKO", "PIOTROWSKI, A", "PIOTROWKSI, MIKIKO" -> "PIOTROWSKI, MIKIKO" // 2nd probably
            else -> prof
        }

        "01:574" -> when (prof) {
            "CHUN, HEE" -> "CHUNG, JAE"
            "MEDINA, JENNIFER", "MEDINA, JENNY", "WANG, MEDINA" -> "WANG-MEDINA, JENNY"
            "PARK, SUNMIN" -> "PARK, SUNGMIN"
            else -> prof
        }

        "01:580" -> when (prof) {
            "FISHER, JAY" -> "FISHER, JOHN" // 01:190, 01:490, 01:615
            else -> prof
        }

        "01:590" -> when (prof) {
            "ROCHA, GIESA" -> "ROCHA, GEISA"
            "STEPHENS, TOM" -> "STEPHENS, THOMAS"
            else -> prof
        }

        "01:595" -> when (prof) {
            "ALONSO, BEJARANO" -> "ALONSO, CAROLINA" // 01:050
            "DINZEY-FLORES, ZAIRE", "DINZEY-FLORES, Z", "DINZEY-FLORES" -> "DINZEY, ZAIRE" // 01:920
            "DUCHESNE" -> "DUCHESNE-SOTOMAYOR, DAFNE"
            "FIGUEROA, YOIMARA" -> "FIGUEROA, YOMAIRA"
            "GARCIA, WILLIAM" -> "GARCIA-MEDINA, WILLIAM"
            "LAURIA, SANTIAG", "LAURIA, SANTIAGO", "LAURIA, ALDO", "LAURIA-SANTIAG, ALDO", "LAURIA-SANTIAG, A", "LAURIA-SANTIAG" -> "LAURIA-SANTIAGO, ALDO"
            "LOPEZ, KATHY" -> "LOPEZ, KATHLEEN" // probably / 01:090
            "MALDONADO-TORR, NELSON", "MALDONADO, TORRES" -> "MALDONADO-TORRES, NELSON"
            "MARTINEZ-SAN, MIGUEL", "MARTINEZ, E" -> "MARTINEZ-SAN, YOLANDA" // 2nd probably / 01:050, 01:195
            "STEPHENS, M" -> "STEPHENS, THOMAS"
            else -> prof
        }

        "01:615" -> when (prof) {
            "DELACY, PAUL" -> "DE LACY, PAUL"
            "FISHER, JAY" -> "FISHER, JOHN" // 01:190, 01:490, 01:580, 01:615
            "HOUGHTEN, P" -> "HOUGHTON, PAULA" // probably
            "SYRETT, KRISTIN" -> "SYRETT, KRISTEN"
            else -> prof
        }

        "01:640" -> when (prof) {
            "NUNZIANTE, DIANA", "BAHRI, NUNZIANTE" -> "BAHRI-NUNZIANTE, DIANA"
            "BALASUBRAMANIA, MOULIK", "KALLUPALAM, BALASUBRAMANIAN", "KALLUPALAM, BALASUBRAM", "KALLUPALAM, MOULIK", "BALASUBRAMANIAN, MOULIK" -> "BALASUBRAM, MOULIK"
            "BARRETO, V" -> "BARRETO-ARANDA, VICTOR"
            "BEALS, MICHAEL", "BEALS, M" -> "BEALS, ROBERT"
            "COHEN, AMY" -> "COHEN-CORWIN, AMY"
            "ECHEVERRIA, ECHEVERRIA" -> "ECHEVERRIA, MARIANO"
            "EVANS, JUDY" -> "EVANS, JUDITH"
            "FINKLESTEIN, JOSHUA" -> "FINKELSTEIN, JOSHUA"
            "FISHER, RANDY" -> "FISHER, RANDOLPH"
            "GAMEIRO-, FUZETO" -> "GAMEIRO-FUZETO, MARCIO"
            "GAMOVA, H" -> "GAMOVA, ELENA"
            "GINDIKIN, SEMEN" -> "GINDIKIN, SIMON"
            "HUANG, XUIAOJUN" -> "HUANG, XIAOJUN"
            "MUKHERJEE, ARJUN" -> "MUKHERJEE, ARUN"
            "PATEL, DAN", "PATEL, DHAN" -> "PATEL, DHANSUKH"
            "SANCHEZ, TAPIA" -> "SANCHEZ-TAPIA, CYNTHIA"
            "SARGYSAN, GRIGOR" -> "SARGSYAN, GRIGOR"
            "SPRAGUE, GABRIELLE" -> "SPRAGUE, GABRIELA"
            "SUSSMAN, HECTOR" -> "SUSSMANN, HECTOR"
            "THELFALL, SHAWN", "THRELLFALL, SHAWN" -> "THRELFALL, SHAWN"
            "TRALDI, -", "TRALDI, ELIANE", "ZERBETTO, -" -> "ZERBETTO, ELIANE"
            "TSIPENYUK, NATALY", "TSIPENYUK, NATALIE" -> "TSIPENYUK, NATALYA"
            else -> prof
        }

        "01:667" -> when (prof) {
            "DIBATTISTA, ANTONY", "DI, BATTISTA" -> "DIBATTISTA, ANTHONY"
            else -> prof
        }

        "01:685" -> when (prof) {
            "ABDELJABER, ABDELHAMID" -> "ABDELJABER, HAMID" // 01:790
            "HAGHANI, FAKHRI" -> "HAGHANI, FAKHROLMOLOUK" // 01:090, 01:988
            "KHAYYAT, EFE", "EFE, E" -> "KHAYYAT, EMRAH" // 01:013, 01:090, 01:195
            "RATZMAN, ELI" -> "RATZMAN, ELLIOT" // 01:508, 01:563
            "RUSSELL-JONES, SANDY", "RUSSELL, SANDRA", "RUSSELL, JONES" -> "RUSSELL-JONES, SANDRA" // 508
            "WEIRECH" -> "WEIRICH"
            else -> prof
        }

        "01:694" -> when (prof) {
            "EDERY, ISSAC" -> "EDERY, ISAAC"
            "GU, S" -> "GU, GUOPING" // probably
            "MATSUMURA, FUMIC" -> "MATSUMURA, FUMIO"
            "MEAD, PARENT" -> "MEAD, JANET"
            "PADGET, R" -> "PADGETT, RICHARD"
            "ZARATIEGUI, BIURRUN", "ZARATIEQUI, MIGUEL" -> "ZARATIEGUI, MIGUEL"
            else -> prof
        }

        "01:730" -> when (prof) {
            "CHERYL, BRADEN" -> "BRADEN, CHERYL"
            "DI, SUMMA-KNOOP", "DISUMMA-KNOOP, L" -> "DI SUMMA-KNOOP, LAURA"
            "EGAN, MARY" -> "EGAN, FRANCES"
            "KALEF, JUSTIN", "KALEF, J" -> "KALEF, PETER"
            "KANG, SUNG" -> "KANG, STEVEN"
            "LEPORE, ERNIE" -> "LEPORE, ERNEST"
            "MARCELLO, ANTOSH" -> "ANTOSH, MARCELLO"
            "MCCELLION, T" -> "MCCELLION, LAVARIS" // probabl
            "MCCROSSIN, TRIP", "MCCROSSIN, T", "MCCROSSIN, EDWAD" -> "MCCROSSIN, EDWARD" // 01:090
            "RATZMAN, ELI" -> "RATZMAN, ELLIOT" // other places
            "SIDER, TED" -> "SIDER, THEODORE"
            else -> prof
        }

        "01:750" -> when (prof) {
            "CHANT, B" -> "CHANT, ROBERT"
            "CHEONG, S-W" -> "CHEONG, SANG-WOOK"
            "CHOU, JP" -> "CHOU, JOHN"
            "DIACONESCU, E", "DIACONSECU, E" -> "DIACONESCU, DUILIU"
            "GERHSTEIN, Y" -> "GERSHTEIN, YURI"
            "HARMON, S" -> "HARMAN, S"
            "KIRYUKIN, VALERY", "KIRKYUKHIN, V" -> "KIRYUKHIN, VALERY"
            "LEE, SH" -> "LEE, SANG-HYUK"
            "MALLIARIS, CONSTANTIN" -> "MALLIARIS, TED"
            "PRYOR, T" -> "PRYOR, CARLTON"
            "ZIMMERMAN, F", "ZIMMERMAN" -> "ZIMMERMANN, FRANK"
            else -> prof
        }

        "01:790" -> when (prof) {
            "ABDELJABER, ABDELHAMID" -> "ABDELJABER, HAMID" // 01:685
            "BATHORY, DENNIS" -> "BATHORY, PETER"
            "BIZZOCO, NIKKI", "BIZOCCO" -> "BIZZOCO, NICOLE"
            "CARNEY-WATERTO", "WATERTON, JO-LEO" -> "CARNEY-WATERTON, JO-LEO"
            "HARRISON, EWAN" -> "HARRISON, RICHARD"
            "KELEMEN, DAN", "KELEMEN, D" -> "KELEMEN, ROGER"
            "MIDLARSKI" -> "MIDLARSKY, MANUS"
            "PETURRSON, SVANUR" -> "PETURSSON, SVANUR" // 01:360
            "PRICE, MELAYNE" -> "PRICE, MELANYE"
            "RESTREPO, SANIN" -> "SANIN, JULIANA"
            "ROSSI, MIKE" -> "ROSSI, MICHAEL"
            "SOCHA, BAILEY" -> "EAISE, BAILEY" // married I think
            "TERENCE, TEO" -> "TEO, TERENCE"
            else -> prof
        }

        "01:810" -> when (prof) {
            "TAMANAHA, DAIANE" -> "TAMANAHA DE QUADROS, DAIANE"
            else -> prof
        }

        "01:830" -> when (prof) {
            "BOYCE, JACINO" -> "BOYCE-JACINO, C"
            "CHANG, A" -> "CHANG, QING"
            "BIESZCZAD, KATARZYNA" -> "BIESZCZAD, KASIA"
            "LYRA, STEIN" -> "STEIN, LYRA"
            "NICOLAS, FERREIRA" -> "NICOLAS, GANDALF"
            "NORTAN, E" -> "NORTAN, S"
            else -> prof
        }

        "01:840" -> when (prof) {
            "BALLENTINE, DEBSA" -> "BALLENTINE, DEBRA"
            "BISHOP, KAHTLEEN" -> "BISHOP, KATHLEEN"
            "LAMMERTS, CHRISTIAN", "LAMMERTS, LAMMERTS", "LAMMERTS MIKAYLA" -> "LAMMERTS, DIETRICH" // 3rd probably
            "RUSSELL, JONES", "RUSSELL, SANDRA", "RUSSELLJONES, SANDRA" -> "RUSSEL-JONES, SANDRA"
            "SUROWITZISRAEL, HILIT", "SUROWITZ, ISRAEL" -> "SUROWITZ, HILIT"
            else -> prof
        }

        "01:860" -> when (prof) {
            "KITZINGER, CHLOE", "KITZINGER, CHLOË" -> "KITZINGER-SHEDLOCK, CHLOE"
            "MEDVEDEVA, NATALIE" -> "MEDVEDEVA, NATALIA"
            else -> prof
        }

        "01:920" -> when (prof) {
            "DINZEY-FLORES, ZAIRE", "DINZEY-FLORES, Z" -> "DINZEY, ZAIRE" // 01:595
            "ELEANOR, LAPOINTE" -> "LAPOINTE, ELEANOR"
            "SMITH, DR" -> "SMITH, DAVID"
            "SONG, K" -> "SONG, EUNKYUNG" // probably
            "WILLHELMS, JEFFREY" -> "WILHELMS, JEFFREY"
            else -> prof
        }

        "01:950" -> when (prof) {
            "COSLOY, J" -> "HECHT-COSLOY, JAIME" // 01:377
            else -> prof
        }

        "01:940" -> when (prof) {
            "ANDREW, VILLADA" -> "VILLADA, ANDREW"
            "BONNIE, BUTLER" -> "BUTLER, BONNIE"
            "CHIVUKULA, DAYCI" -> "CHIVUKULA, LUCRECIA"
            "KAREN, SANCHEZ" -> "SANCHEZ, KAREN"
            "KIM, Y-S" -> "KIM, YEON-SOO"
            "PEREZ, CORTES" -> "PEREZ-CORTES, SILVIA"
            "SANCHEZ, INOFUENTES", "SANCHEZ-INOFUENTES, CELSO", "SANCHEZ-INOFUENTES, CELS", "SANCHEZ-INOFUENTES, C" -> "SANCHEZ, CELSO"
            "STEPHENS, TOM" -> "STEPHENS, THOMAS"
            "VILLALBA, ROSADO" -> "VILLALBA, CELINES"
            else -> prof
        }

        "01:960" -> when (prof) {
            "DEOKI, SHARMA" -> "SHARMA, DEOKI"
            "DONG, HK" -> "DONG, HEI-KI"
            "GEORGE, POPEL" -> "POPEL, GEORGE"
            "JOE, NAUS", "NAUS, JOSEPH" -> "NAUS, JOE"
            "LMIELINSKI, T" -> "IMIELINSKI, TOMASZ" // 01:198
            "LUO, TIANHOA" -> "LUO, TIANHAO"
            "LYNN, AGRE" -> "AGRE, LYNN"
            "MARDEKAIN, JACK" -> "MARDEKIAN, JACK"
            "MICHAEL, MINIER" -> "MINIERE, MICHAEL"
            "NILSEN, MILLER" -> "MILLER, NILSEN"
            "ROJAS, PATRICK" -> "ROJAS, PATRICIO"
            "SHEILA, LAWRENC", "S, LAWRENCE" -> "LAWRENCE, SHEILA"
            "SHYAM, MOONDRA" -> "MOONDRA, SHYAM"
            "ZHANYUN, ZHAO" -> "ZHAO, ZHANYUN"
            else -> prof
        }

        "01:988" -> when (prof) {
            "ALEXANDER-FLOY, N", "ALEXANDER-FLOY" -> "ALEXANDER-FLOYD, NIKOL" // 01:090
            "BALAKRISHNAN, RADIKA" -> "BALAKRISHNAN, RADHIKA"
            "BENBOW, CANDACE" -> "BENBOW, CANDICE"
            "BENSETTI, BENBADER" -> "BENSETTI-BENBADER, HAYET"
            "BURKE", "BURK, TARA" -> "TARA, BURK"
            "CIPRIANI, SASXIA" -> "CIPRIANI, SASKIA"
            "COBBLE, S" -> "COBBLE, DOROTHY"
            "EASLEY-HOUSER, M" -> "EASLEY-HOUSER, ARIKA"
            "FLITTERMANLEWIS, SANDY" -> "FLITTERMAN-LEWIS, SANDY" // 01:354
            "GRIFFEN" -> "GRIEFEN, KAT"
            "HAGHANI, FAKHRI", "HAGHANI, FAKARI" -> "HAGHANI, FAKHROLMOLOUK"
            "HEIDI, HOECHST" -> "HOECHST, HEIDI"
            "HETFIELD, LISA", "HETFIELD, LIST" -> "HETFIELD, ELIZABETH"
            "JULIA, WARTENBERG", "WARTENBURG, JULIA", "WARTENBURG, J" -> "WARTENBERG, JULIA"
            "RAJAN, JULIS" -> "RAJAN, JULIE"
            "SANON, JULES" -> "SANON-JULES, LISA"
            "RUSSELL, JONES", "RUSSELL, SANDRA", "RUSSELL-JONES" -> "RUSSEL-JONES, SANDRA" // other places too
            "SCIBELLI, STINA" -> "SODERLING, STINA"
            "SHAHID, SHALEENA" -> "SHAHID, SHAHEENA"
            else -> prof
        }

        "01:991" -> when (prof) {
            "SANCHEZ-INOFUENTES, CELSO" -> "SANCHEZ, CELSO"
            else -> prof
        }

        "03:690" -> when (prof) {
            "CAPT, J", "MURRAY, CAPT" -> "MURRAY, JANICE"
            "CAPT, GONZALEZ" -> "GONZALEZ, KEVIN"
            "CAPT, MANNING" -> "MANNING, OMAR"
            "DANYLUK, LT COL", "LT COL, DANYLUK" -> "DANYLUK, DEBORAH"
            "MARINELLI, CAPT" -> "MARINELLI, VINCENT"
            "MCANDREW, LT COL", "LT COL, MCANDRE" -> "MCANDREW, MATTHEW"
            "STEVENS, CAPT", "CAPT, K" -> "STEVENS, KAYLA"
            else -> prof
        }

        "03:691" -> when (prof) {
            "LOY, MAJOR" -> "LOY, STUART"
            "VENDETTI, ERIC", "VENDITTI, CAPLAIN" -> "VENDITTI, ERIC" // 2nd probably
            else -> prof
        }

        "03:692" -> when (prof) {
            "FORSYTH, LT" -> "FORSYTH, SEAN" // probably
            "PATRICK, CDR" -> "PATRICK, HADEN" // probably
            "RAMEY, CPT" -> "RAMEY, CAPT"
            else -> prof
        }

        "04:189" -> when (prof) {
            "DOOL, RAYMOND" -> "DOOL, RICHARD" // 04:192
            "KANG, KYUNGWON" -> "KANG, KATIE"
            "LINARDOPOULOS, NICK" -> "LINARDOPOULOS, NIKOLAOS" // 04:192
            "LISCHER-KATZ, ZACK" -> "LISCHER-KATZ, ZACHARIAH"
            "MCCORMICK, PHILLIP" -> "MCCORMICK, PHILIP"
            "PICHIGUN, ALEX" -> "PICHUGIN, ALEXANDER" // 04:547
            "STOERGER, SHARN" -> "STOERGER, SHARON"
            else -> prof
        }

        "04:192" -> when (prof) {
            "AAHKUS, MARK" -> "AAKHUS, MARK"
            "BAIA, CHIS" -> "BAIA, CHRIS"
            "DOOL, RAYMOND" -> "DOOL, RICHARD" // 04:189
            "DYWER, MARIA" -> "DWYER, MARIA"
            "ENTER, ROBERTA" -> "ENTNER, ROBERTA"
            "GIGLIOTTI, RALPH" -> "GIGLIOTTI, RAFFAELE"
            "GRASSO, JACK" -> "GRASSO, JOHN"
            "HERKY, PETER" -> "HERCKY, PETER"
            "HOUSEHOLDER, BRAIN" -> "HOUSEHOLDER, BRIAN"
            "LAKHANI, DOLLY" -> "LAKHANI, DAULAT"
            "LINARDOPOULOS, NICK" -> "LINARDOPOULOS, NIKOLAOS" // 04:189
            "MOKROS, HARTY" -> "MOKROS, HARTMUT"
            "NOGUIERA, K" -> "NOGUEIRA, KATHRYN"
            "YOUN, HUNSOOK" -> "YOUN, HYUNSOOK"
            else -> prof
        }

        "04:547" -> when (prof) {
            "BAKELAAR, PHILLIP" -> "BAKELAAR, PHILIP"
            "BARCLAY-PLATEN, LEANNE" -> "BARCLAY-PLATENBURG, LEANNE"
            "BELKIN, NICK" -> "BELKIN, NICHOLAS"
            "BINDE, BINDE" -> "BINDE, BETH"
            "COLLICK, CHARLI" -> "COLLICK, CHARLES"
            "DOYLE, MIKE" -> "DOYLE, MICHAEL"
            "KERSCHNER, JR", "KERSCHNER, CHUCK" -> "KERSCHNER, CHARLES"
            "O', CONNOR" -> "O'CONNOR, DANIEL"
            "PICHIGUN, ALEX" -> "PICHUGIN, ALEXANDER" // 04:189
            "POTOCKI, ADAM" -> "POTOCKI, RADOSLAW"
            "SANCHEZ, JOE", "JOSE, SANCHEZ" -> "SANCHEZ, JOSE"
            "UPDALE, BILL" -> "UPDALE, WILLIAM"
            "WACHOLDER, NINA" -> "WACHOLDER, FAYE"
            "WILLOUGHBY-LIT, SHAKIRA" -> "WILLOUGHBY-LITTLE, SHAKIRA"
            "ZHOU, XIAOMU" -> "ZHOU, XIAMOU"
            else -> prof
        }

        "04:567" -> when (prof) {
            "ARONCYZK, MELISSA" -> "ARONCZYK, MELISSA"
            "D'AMBRISIO, MARY" -> "D'AMBROSIO, MARY"
            "DAVIS, TOM" -> "DAVIS, THOMAS"
            "FELDMAN, ROGERS" -> "FELDMAN, LAUREN"
            "FITZPATRICK, MICHEAL" -> "FITZPATRICK, MICHAEL"
            "FUERST, LIZ" -> "FUERST, ELIZABETH"
            "KALET, HANK" -> "KALET, HENRY" // probably
            "MCCARTHY, MIKE" -> "MCCARTHY, MICHAEL"
            "PAVLICHKO, MIKE" -> "PAVLICHKO, MICHAEL"
            "RISPOLI, MIKE" -> "RISPOLI, MICHAEL"
            "STRUPP, JOE" -> "STRUPP, JOSEPH"
            else -> prof
        }

        "05:300" -> when (prof) {
            "ANDREW, LELAND" -> "LELAND, ANDREW"
            "ASHA, NAMBIAR" -> "NAMBIAR, ASHA"
            "BOTT, CYNTHIA" -> "BOTT-TOMARCHIO, CYNTHIA"
            "CUPELLO-WATERS, ALEXANDRA", "CUPELLO, WATERS", "CUPELLO-WATERS" -> "CUPELLO, ALEXANDRA"
            "CYNTHIA, PANCER", "PANCER, CINDY" -> "PANCER, CYNTHIA"
            "DELNERO, J" -> "DEL NERO, J"
            "DEHAM-BARRETT" -> "DENHAM-BARRETT, MISTY"
            "DOUGHTERTY, S" -> "DOUGHERTY, S" // probably, might b flipped
            "GOLDIN, JERRY" -> "GOLDIN, GERALD"
            "ELIZABETH, VASTANO" -> "VASTANO, ELIZABETH"
            "EVAN, JAFFE" -> "JAFFE, EVAN"
            "JAMES, LYNCH-URBANIAK", "URBANIAK, JAMES", "URBANIAK" -> "LYNCH-URBANIAK, JAMES"
            "JAMES, O'KELLY" -> "O'KELLY, JAMES"
            "KNOX, LISA" -> "KNOX-BROWN, LISA"
            "LAZZARRO, HEATHER", "LAZZARRO, H" -> "LAZZARO, HEATHER"
            "MAUCLAIRAUGUST, BRANDON", "MAUCLAIR-AUGUS" -> "MAUCLAIR-AUGUSTIN, BRANDON"
            "MCHUGH, SANDEE" -> "MCHUGH-MCBRIDE, SANDEE"
            "NOLEN, RYAN" -> "NOLEN, JIMMY" // maybe flipped
            "PALPACUER-LEE, CHRISTELLE", "PALPACUER-LEE" -> "PALPACUER, CHRISTELLE"
            "RANDALL, WEEKS" -> "RANDALL-WEEKS, MAQUEDA"
            "WITCHEL, SUZANNE" -> "WICHTEL, SUZANNE"
            "WOODWARD, KARIMA" -> "WOODYARD, KARIMA"
            "DAKE, ZHANG" -> "ZHANG, DAKEN"
            else -> prof
        }

        // Random uncompleted things
        "07:080" -> when (prof) {
            "DICKNISON, BRENT" -> "DICKINSON, BRENT"
            "PETERSON, ROB" -> "PETERSEN, ROBERT"
            else -> prof
        }

        "07:081" -> when (prof) {
            "AKIN, ATIF" -> "AKIN, AHMET"
            "BEEGAN, GERRY" -> "BEEGAN, GERALD"
            "BRITTINGHAM, JIM" -> "BRITTINGHAM, JAMES"
//            "DADABHOY, JACK" -> "DADABHOY, NABILA" // probably swapped first name / or "HOGAN, JACK"? (or both)
//            "DEGAETANI, BRITTINGHA" -> "DEGAETANI, KATHERINE" // or "BRITTINGHAM, JAMES"? (or both)
            // I think "DADABHOY, JACK" and "DEGAETANI, BRITTINGHA" both  have non-matching first/last names
            "EDGARTON, BRIAN" -> "EDGERTON, BRIAN"
            "FIGUERDO, ENRIQUE" -> "FIGUEREDO, ENRIQUE"
            "FRANCIR, LINDA" -> "FRANCIS, LINDA"
            "MALISZEWSKI, BETTY" -> "MALISZEWSKI, MARY" // probably
            else -> prof
        }

        "08:081" -> when (prof) {
            "OLESON JEANNIE" -> "OLESON, JEANINE"
            else -> prof
        }

        "14:180" -> when (prof) {
            "TREFOR, WILLIAMS" -> "WILLIAMS, TREFOR"
            else -> prof
        }

        "14:332" -> when (prof) {
            "BANGTIAN, LIU" -> "LIU, BANGTIAN"
            "BURDEA, GREG" -> "BURDEA, GRIGORE"
            "CAN, LIU" -> "LIU, CAN"
            "JHA, SHANTANU" -> "JHA, SHANTENU"
            "KARIMINI, NAHHMEH" -> "KARIMI, NAGHMEH"
            "PARAKEVAKOS, IOANNIS" -> "PARASKEVAKOS, IOANNIS"
            "SPASOJEVIC, PREDRAY" -> "SPASOJEVIC, PREDRAG"
            "SUBRAMANIAN, NAGI" -> "SUBRAMANIAN, NAGANATHAN"
            else -> prof
        }

        "14:440" -> when (prof) {
            "ALEJANDRO, RUIZ" -> "RUIZ, ALEJANDRO"
            "BARBARA, PORTER" -> "PORTER, BARBARA"
            "EUIHRARK, LEE" -> "LEE, EUIHARK"
            "HAROLD, BENNETT" -> "BENNETT, HAROLD"
            "GUIDO, SCHMITZ" -> "SCHMITZ, GUIDO"
            "MARTIN, GOLDEN" -> "GOLDEN, MARTIN"
            "MEHRNAZ, TAVAN" -> "TAVAN, MEHRNAZ"
            "MOGHATADERNEJAD, SARA" -> "MOGHTADERNEJAD, SARA"
            "WON-JONG, RHEE" -> "RHEE, WON-JONG"
            else -> prof
        }

        "14:540" -> when (prof) {
            "JEONG, MK" -> "JEONG, MYONG-KEE"
            "NEMATI, PROON" -> "NEMATI, SEPHR"
            "WANG, HONGANG" -> "WANG, HONGGANG"
            else -> prof
        }

        "14:635" -> when (prof) {
            "AKDOGAN, K", "AKDOGAN, KORAY" -> "AKDOGAN, ENVER"
            "MATTHEWSON, JOHN", "MATTHEWSON, J" -> "MATTHEWSON, MICHAEL"
            else -> prof
        }

        "14:650" -> when (prof) {
            "DIEZ, GARIAS", "DIEZ, F" -> "DIEZ-GARIAS, FRANCISCO"
            else -> prof
        }

        "19:910" -> when (prof) {
            "ALI, MARY", "ALI, BETH" -> "ALI, MARY BETH"
            "AMANDA, THOMPSON" -> "THOMPSON, AMANDA"
            "AMANDA, MATHISEN" -> "MATHISEN, AMANDA"
            "ANGELL, BETH", "ANGELL, B" -> "ANGELL, KATHRYN"
            "ANTHONY, VIVIEN" -> "ANTHONY, WEN"
            "BARNETT, C" -> "BARNITT, C"
            "BERGACS, KATIE" -> "BERGACS, KATHARINE"
            "BOAINAELLI, L" -> "BOIANELLI, L"
            "COHEN, EMILY", "GREENFIELD, COHEN" -> "GREENFIELD, EMILY"
            "COANGELO, B" -> "COLANGELO, BRIAN"
            "CARL, SIEBERT" -> "SIEBERT, CARL"
            "DARCY, SIEBERT" -> "SIEBERT, DARCY"
            "DAVID, BARRY" -> "BARRY, DAVID"
            "DAVID, A" -> "ERRICKSON, DAVID"
            "REBECCA, DAVIS" -> "DAVIS, REBECCA"
            "DEMAIO, KATHY" -> "DEMAIO, KATHLEEN"
            "DURON, JACKIE" -> "DURON, JACQUELYNN"
            "FARMER" -> "FARMER, ANTOINETTE" // !! This applies only for Fall 2016 entry !!
            "FESTA, SANDY" -> "FESTA, SANDRA"
            "KERENZA, REID" -> "REID, KERENZA"
            "GAUGHAN, LINDSEY" -> "GAUGHAN, LINDSAY" // probably
            "CHARLES, GOLDSTEIN" -> "GOLDSTEIN, CHARLES"
            "ANALEAH, GREEN" -> "GREEN, ANALEAH"
            "HALEY-LOCK, ANNA", "HALEY-LOCK, A" -> "HALEY, ANNA"
            "HEATHER, BURROUGHS" -> "BURROUGHS, HEATHER"
            "MILLS-PEVONIS, HEATHER" -> "MILLS, HEATHER"
            "HODGON, B" -> "HODGDON, B"
            "GRETCHEN, HOGE" -> "HOGE, GRETCHEN"
            "CHIEN-CHUN, HUANG" -> "HUANG, CHIEN-CHUN"
            "JOO, MICHAEL" -> "JOO, MYUNG-KOOK"
            "KAZHAROVA-KACZ, O", "KACZMARCYZK, O" -> "KAZHAROVA-KACZMARCZYK, OKSANA"
            "KIM, JEOUNG-HEE" -> "KIM, JOY"
            "MANAHAN, MOORE", "MOORE, MANAHAN" -> "MOORE MANAHAN, GERALDINE"
            "BRANDI, KOHR" -> "KOHR, BRANDI"
            "LEVENTHAL, JODI", "LEVENTHAL, J" -> "LEVINTHAL, J"
            "LOMANGINO-DIMA, D", "DIMAURO, D", "LOMANGUNO, D" -> "LOMANGINO-DIMAURO, DAWNE"
            "LAURA, LUCIANO" -> "LUCIANO, LAURA"
            "MANDA, GATTO" -> "GATTO, MANDA"
            "JANINE, MARISCOTTI" -> "MARISCOTTI, JANINE"
            "COLLEEN, MARTINEZ" -> "MARTINEZ, COLLEEN"
            "MASON-ANDRES, BEVERLY" -> "MASON, BEVERLY"
            "MAYERS, SANCHEZ", "MAYERS, RAYMOND", "RAY, SANCHEZ", "SANCHEZ, MAYERS", "SANCHEZ-MAYERS, R" -> "SANCHEZ MAYERS, RAYMOND"
            "MCMAHON-CANNIZZO, SARAH" -> "MCMAHON, SARAH"
            "BEMBRY, N" -> "MOORE-BEMBRY, NATALIE"
            "CEDENO-MORRIS, NANCY" -> "MORRIS, NANCY"
            "NATHAN, RANDY" -> "NATHAN, RANDALL"
            "STAMM, RANDY", "RANDALL, STAMM" -> "STAMM, RANDALL"
            "NICELY-COLANGE, K", "COLANGELO, K" -> "NICELY-COLANGELO, KRISTIN"
            "NICOLE, MORELLA" -> "MORELLA, NICOLE"
            "PATTERSON, SESSOMES", "SESSIMONE-PATT, D" -> "PATTERSON-SESSOMES, D"
            "PETERSON, A" -> "PETERSON, NELSON"
            "PHILLIPS, JACQUELINE", "PHILLIPS, JACKIE" -> "PHILLIPS, JACQULINE"
            "JOYCE, PRIOR" -> "PRIOR, JOYCE"
            "RAMOS, B", "RAMOS, BIANCA" -> "CHANNER, BIANCA"
            "RINALDI, CHERYL" -> "TORRES, CHERYL" // probably
            "ROBLES, GABRIEL", "ROBLES, ALBERTO" -> "ROBLES ALBERTO, GABRIEL"
            "RONALD, QUINCY" -> "QUINCY, RONALD"
            "DONALD, STAGER" -> "STAGER, DONALD"
            "SADHWANI-MONCH, D", "SANHWANI, D" -> "SADHWANI-MONCHAK, DEEPA"
            "SALERNO, E" -> "SALERNO, L" // probably same person, idk who
            "LA'TESHA, SAMPSON" -> "SAMPSON, LATESHA"
            "NANCY, SCHLEY" -> "SCHLEY, NANCY"
            "RACHEL, SCHWARTZ" -> "SCHWARTZ, RACHEL"
            "SCOTTO-ROSATO, NANCY", "SCOTTO, NANCY" -> "SCOTTO-ROSATO, NUNZIA" // strong maybe
            "SHRIVRIDHI, SHUKLA" -> "SHUKLA, SHRIVRIDHI"
            "KARUN, SINGH" -> "SINGH, KARUN"
            "SMITH, D" -> "SMITH, DANIEL" // !! only for F16-F17 !!
            "SMITH, J", "SMITH, MELISSA", "LISA, SMITH" -> "SMITH, LISA" // both maybe; also 1st might be "SINHA, J"
            "SONIA, BROWN" -> "BROWN, SONIA"
            "GERI, SUMMERS" -> "SUMMERS, GERI"
            "WALDMAN, B" -> "WALDMAN, WILLIAM"
            "WHITFIELD-SPIN, L", "WHITFELD-SPINN, L", "WHITTFIELD-SPINNER, L" -> "WHITFIELD-SPINNER, LINDA"
            "WILKENFIELD, B" -> "WILKENFELD, BONNIE"
            "CHRISTINE, JAMES" -> "JAMES, CHRISTINE" // probably
            "DELUCA, LITKEY", "DELUCCA-LITKEY, A" -> "DELUCA-LITKEY, AMY"
            "VANALST, D" -> "VAN ALST, D"
            else -> prof
        }

        "33:010" -> when (prof) {
            "MOFFIT, KEVIN" -> "MOFFITT, KEVIN"
            "WASERMAN, EVAN" -> "WASSERMAN, EVAN"
            else -> prof
        }

        "33:011" -> when (prof) {
            "WIRTENBERG, THELMA" -> "WIRTENBERG, JEANA" // 33:620
            else -> prof
        }

        "33:136" -> when (prof) {
            "BEN-ISREAL, A", "ISRAEL, A" -> "BEN-ISRAEL, ADI"
            "BRUNING, TOM" -> "BRUNING, THOMAS"
            "CAVALERIO, M" -> "CAVALEIRO, MARTA"
            "MCGINTY, C" -> "MCGINITY, CURTIS"
            "RODRIGUES, GUARAV" -> "RODRIGUES, GAURAV"
            "SZATROWKSI, T" -> "SZATROWSKI, TED"
            "ZENAROSA, GABRIEL" -> "ZENAROSA, GABE"
            else -> prof
        }

        "33:390" -> when (prof) {
            "KIM, JIN" -> "KIM, DONGCHEOL"
            "LEE, CHENG", "LEE, CHENG-FEW" -> "LEE, CHENG"
            else -> prof
        }

        "33:620" -> when (prof) {
            "SIEGEL, PHYLLIS" -> "SIEGEL-FRIEDMAN, PHYLLIS"
            "WIRTENBERG, THELMA" -> "WIRTENBERG, JEANA" // 33:011
            else -> prof
        }

        "33:630" -> when (prof) {
            "FILPPAZZO, ED" -> "FILIPPAZZO, ED"
            "GIARRATANO, FRANK" -> "GIARRATANO, FRANCIS"
            "MONGA, S" -> "MONGA, ALOKPARNA"
            else -> prof
        }

        "33:799" -> when (prof) {
            "KLEPACKI, DAN", "KLEPACKI, D" -> "KLEPACKI, BOGDAN"
            "LYON, K" -> "LYON, KEVIN"
            "MCLAURY, B" -> "MCLAURY, WILLIAM"
            "SPIEGLE, G" -> "SPIEGLE, EUGENE"
            else -> prof
        }

        "50:014" -> when (prof) {
            "IBN-ZIYAD, MAHDI", "IBN-ZIYAD" -> "ZIYAD, MAHDI" // 50:840
            "HAZZARD, KATRINA" -> "HAZZARD-DONALD, KATRINA" // 50:070, 50:920
            else -> prof
        }

        "50:070" -> when (prof) {
            "COE, CATI" -> "COE, CATHLEEN"
            "HAZZARD, KATRINA", "HAZZARD" -> "HAZZARD-DONALD, KATRINA" // 50:014, 50:920
            else -> prof
        }

        "50:080" -> when (prof) {
            "DEMARAY, PROF" -> "DEMARAY, ELIZABETH"
            "ESPIRITU, ALAN" -> "ESPIRITU, ALLAN" // 50:082
            "BRUCE, GARRITY" -> "GARRITY, BRUCE"
            "FLIBERT, JEFFREY", "FILBERT, JEFFERY" -> "FILBERT, JEFFREY"
            "JOHNSON, TONY", "JOHNSON, HAMILITON" -> "JOHNSON, HAMILTON"
            "LEDONNE, NICK" -> "LEDONNE, NICHOLAS"
            "LEECH, KATIE" -> "LEECH, MARY" // probably
            "SHPANIN, STANISLAW" -> "SHPANIN, STANISLAV"
            "SYMERSKI, ANTHONY" -> "SMYRSKI, ANTHONY"
            else -> prof
        }

        "50:082" -> when (prof) {
            "BRUCE, GARRITY" -> "GARRITY, BRUCE" // 50:080
            "HEITLER, GABY" -> "HEITLER, GABRIELLE"
            "PILLIOD, ELIZABETH" -> "PILLIOD, BETH"
            "RODRIGUEZ-LAWSON" -> "RODRIGUEZ, ANABELLE"
            else -> prof
        }

        "50:090" -> when (prof) {
            "CHERYL, HALLMAN" -> "HALLMAN, CHERYL" // 50:202
            else -> prof
        }

        "50:100" -> when (prof) {
            "GAMBS, JERRY" -> "GAMBS, GERARD"
            else -> prof
        }

        "50:120" -> when (prof) {
            "ABDUS-SABOUR, ISHMAIL" -> "ABDUS-SABOOR, ISHMAIL"
            "BORDEN, ZACK" -> "BORDEN, ZACHARY"
            "GONZALEZ, ANGELICA", "GONZALEZ, A" -> "GONZALEZ, GAJARDO"
            "MALCOLM, KATIE" -> "MALCOLM, KATALIN"
            "OBERLE-KILIC, JENNIFER" -> "OBERLE, JENNIFER"
            "SPRINGER, RUTH" -> "SPORER, RUTH"
            else -> prof
        }

        "50:160" -> when (prof) {
            "ARBUCKLE" -> "ARBUCKLE-KEIL, GEORGIA"
            "CHERFANE" -> "SOLIMEO-CHERFANE, RENEE"
            "FAJGIER, CHUCK" -> "FAJGIER, CHARLES"
            "SALAS" -> "SALAS-DE LA CRUZ, DAVID"
            else -> prof
        }

        "50:163" -> when (prof) {
            "LU, WEHNUA" -> "LU, WENHUA"
            "LUPOLD" -> "LUPOLD-KEILEN, EVA"
            else -> prof
        }

        "50:198" -> when (prof) {
            "EGAN, DENNIS" -> "EGEN, DENNIS"
            "GANDHI, TEJAS" -> "GANDHI, RAJIV" // probably
            "PATARCITY, JOE" -> "PATARCITY, JOSEPH"
            "RAMASWAMI, SUNEETS" -> "RAMASWAMI, SUNEETA"
            else -> prof
        }

        "50:202" -> when (prof) {
            "GILLIARD-MATTH" -> "GILLIARD-MATTHEWS"
            "CHERYL, HALLMAN" -> "HALLMAN, CHERYL" // 05:090
            else -> prof
        }

        "50:209" -> when (prof) {
            "BROWN, JB1343" -> "BROWN, JAMES"
            "EMMONS, RAEMMONS" -> "EMMONS, ROBERT"
            else -> prof
        }

        "50:220" -> when (prof) {
            "EMARA, NOMA", "EMARA, NOAH" -> "EMARA, NOHA"
            "MA, JIMPENG" -> "MA, JINPENG"
            "MORELLI, MICHEAL", "MORELLI, MICHALL", "MORELLI, MICHAELI" -> "MORELLI, MICHAEL"
            "PASCALE, GUY" -> "PASCALE, GAETANO" // probably
            "YAMADA, JETSUJI" -> "YAMADA, TETSUJI"
            else -> prof
        }

        "50:350" -> when (prof) {
            "BARBARESE, JT" -> "BARBARESE, JOSEPH"
            "BLACKFORD, MOLLY" -> "BLACKFORD, HOLLY"
            "GUEDON, CHRISTINE", "GUEDONDECON, CHRISTINE", "GUEDONDECONCI, CHRISTINE", "GUEDONDECONCINI, CHRISTINE" -> "GUEDON DE CONCINI, CHRISTINE" // 50:352
            "HABIB, MA", "HABIB, M" -> "HABIB, RAFEY" // 50:525
            "HOSTETTER, ARRON" -> "HOSTETTER, AARON"
            "MOOREHEAD" -> "MOORHEAD, DANIEL"
            "STALNAKER, MATTEW" -> "STALNAKER, MATTHEW"
            "TIMOTHY, MARTIN" -> "MARTIN, TIMOTHY"
            else -> prof
        }

        "50:352" -> when (prof) {
            "ABDUL, JABBAAR" -> "ABDULJABBAAR, MALIK"
            "GUEDON, CHRISTINE", "GUEDONDECON, CHRISTINE", "GUEDONDECONCI, CHRISTINE", "GUEDONDECONCINI, CHRISTINE" -> "GUEDON DE CONCINI, CHRISTINE" // 50:350
            else -> prof
        }

        "50:354" -> when (prof) {
            "MOKHBERI, EMUP", "MOKBERI, EMUD" -> "MOKHBERI, EMUD"
            "MATTHEW, SORRENTO" -> "SORRENTO, MATTHEW"
            else -> prof
        }

        "50:499" -> when (prof) {
            "DUNEAV, JAMIE" -> "DUNAEV, JAMIE"
            else -> prof
        }

        "50:512" -> when (prof) {
            "WOLOSON, WENDF" -> "WOLOSON, WENDY"
            else -> prof
        }

        "50:525" -> when (prof) {
            "HABIB, MA" -> "HABIB, RAFEY" // 50:350
            "WESTMAN, LEANN" -> "WESTMAN, LEEANN"
            else -> prof
        }

        "50:640" -> when (prof) {
            "DONAHU" -> "DONAHUE, S" // probably
            "HERRERA, H", "HERRERA" -> "HERRERA-GUZMAN, HAYDEE"
            "LESHEN, SARA" -> "LESHEN-GROSS, SARA"
            "SANCHIRICO, NICHOLAS" -> "SANCHIRICO, NICK"
            else -> prof
        }

        "50:700" -> when (prof) {
            "ANTONNACCI, JARRED", "JARRED, ANTONACCI" -> "ANTONACCI, JARRED"
            "ARNARSON, STEPHAN", "ARNARSON, STEEAN" -> "ARNARSON, STEFAN"
            "BAIRD, JALIANNE" -> "BAIRD, JULIANNE" // 50:701 kinda
            "LAURIE, LALLY" -> "LALLY, LAURIE"
            "MOLK, DAVID" -> "MOLK, DAVE"
            "ERIC, POLACK" -> "POLACK, ERIC" // 50:701
            "JULIA, ZAVADSKY" -> "ZAVADSKY, JULIA" // 50:701 kinda
            else -> prof
        }

        "50:701" -> when (prof) {
            "BAIRD, JULIANNA" -> "BAIRD, JULIANNE" // 50:700 kinda
            "CIULEIATANASIU, LENUTA" -> "CIULEI, LENUTA"
            "POLACK, ERIK" -> "POLACK, ERIC" // 50:700
            "ZAVADSKY, JYLIA" -> "ZAVADSKY, JULIA" // 50:700 kinda
            else -> prof
        }

        "50:730" -> when (prof) {
            "BETZ, MARGERET" -> "BETZ, MARGARET"
            "SACKS, BRIAN" -> "SACKS, BRYAN"
            "PHILLIPS, YOUNG" -> "YOUNG, PHILLIPS"
            else -> prof
        }

        "50:750" -> when (prof) {
            "BRANNIGAN, GRACE" -> "BRANNIGAN, JESSICA"
            "GRIEPENBERG, JULIANNE", "GRIEPENBURG, JULIANNA" -> "GRIEPENBURG, JULIANNE"
            "SHEINBERG, MICHAEL" -> "SCHEINBERG, MICHAEL"
            "TROUT, COREY" -> "TROUT, CORY" // probably
            else -> prof
        }

        "50:790" -> when (prof) {
            "AYSCUE, STEPHEN" -> "AYSCUE, STEVE"
            "BARTCH, CATHY" -> "BARTCH, CATHERINE"
            "BOWERS, MELONIE" -> "BOWERS, MELANIE"
            "LAMBERT, KATHERYN", "KATHRYN, LAMBERT" -> "LAMBERT, KATHRYN"
            "MACKIEWICZWOLF, WOJTEK", "MACKIEWICZ-WOLFE, WOJIECH", "MACKIEWICZ-WOLFE, WOJCIE", "MACKIEWICZ-WOLFE, WOJCIECH" -> "MACKIEWICZ-WOLFE, WOJTEK"
            "RAYMOND, MASSI" -> "MASSI, RAYMOND"
            "SHAHEEN, AYUBI" -> "AYUBI, SHAHEEN"
            "SHAMES, SHAVNA" -> "SHAMES, SHAUNA"
            else -> prof
        }

        "50:830" -> when (prof) {
            "ALLENDORFER, KENNETH" -> "ALLENDOERFER, KENNETH"
            "BERNARDINI, STEPHAN" -> "BERNARDINI, STEPHEN"
            "CAVANAUGH, COURTNAY" -> "CAVANAUGH, COURTENAY"
            "CHAN, WAGNE" -> "CHAN, WAYNE"
            "CORRY, ANJANNETT" -> "CORRY, ANJENNETT"
            "DIXON, MICHELLE" -> "DIXON, PALMER"
            "DUFFY, SEAN" -> "DUFFY, EDWARD" // or maybe flipped?
            "LATU, IONA" -> "LATU, IOANA"
            "GODOFSKY, LEAVY", "LEAVY, BARBARA" -> "GODOFSKYLEAVY, BARBARA"
            "MELUSO, ANGELA" -> "MELUSO-SCAFIDI, ANGELA"
            "ODONNELL, ALEXANRA", "ODONNELI, ALEXANDRA" -> "O'DONNELL, ALEXANDRA"
            "PAPPAS, ZISSUS" -> "PAPPAS, ZISSIS"
            "ELENA, RAGUSA" -> "RAGUSA, ELENA"
            "VANDERWEL, ROBREHT", "VANDERWEL, ROBRECHT" -> "VAN DER WEL, ROBRECHT"
            else -> prof
        }

        "50:840" -> when (prof) {
            "GILMORE-CLOUGH, KIPP" -> "GILMORE-CLOUGH, GREGORY"
            "GREG, SALYER" -> "SALYER, GREG"
            "IBN-ZIYAD, MAHDI" -> "ZIYAD, MAHDI" // 50:014
            else -> prof
        }

        "50:910" -> when (prof) {
            "CANDELARIA, E", "ELSA, CANDELARIO" -> "CANDELARIO, ELSA"
            "CALAMERE, M" -> "CARLAMERE, MICHELLE"
            "MANAHAN, MOORE" -> "MOORE, MANAHAN"
            "MARLA, BLUNT-CARTER" -> "BLUNT-CARTER, MARLA"
            "LORI, SCHLOSSER" -> "SCHLOSSER, LORI"
            "SECCHUITTI, J", "SECCHUTTI, J", "JUDITH, SECCHIUTTI" -> "SECCHIUTTI, JUDITH"
            "STANGLIANO, R", "RICHARD, STAGLIANO" -> "STAGLIANO, RICHARD"
            "SUSAN, HIGGINS" -> "HIGGINS, SUSAN"
            "TAYLOR-BASHSHA, J" -> "TAYLOR-BASHSHAR, JESSICA"
            else -> prof
        }

        "50:920" -> when (prof) {
            "COHEN, KALLAN" -> "COHEN, JOANNA"
            "HAZZARD, KATRINA", "HAZZARD" -> "HAZZARD-DONALD, KATRINA" // 50:014, 50:014
            "HERRSCRAFT-ECKM, BRYN", "HERRSCHAFT" -> "HERRSCHAFT-ECKMAN, BRYN"
            else -> prof
        }

        "50:940" -> when (prof) {
            "CASTILLIO, MAURICIO" -> "CASTILLO, MAURICIO"
            "MOSSVELASCO, DONIE", "VELASCO, DANIE" -> "MOSS-VELASCO, DANIE"
            "PEREZ, CORTES" -> "PEREZ-CORTES, SILVIA"
            "PROSPERO, GARCIA" -> "GARCIA, PROSPERO"
            "RODGERS, MADISON" -> "ROGERS, MADISON"
            "SANTOS, LORENA" -> "SANTOS, QUINONES"
            else -> prof
        }

        "50:964" -> when (prof) {
            "ANN, HEIDELBERG" -> "HEIDELBERG, ANN"
            "ONEILL, KRISTIN", "KRISTIN, O'NEIL", "O'NEILL" -> "O'NEIL, KRISTIN"
            "PARKS" -> "PARK, DEBRA"
            "PIDGEON, N" -> "WERNER-PIDGEON, NICOLE"
            "JOHN, ROTHWELL" -> "ROTHWELL, JOHN"
            "SANTO, TOM" -> "SANTO, THOMAS"
            "SARA, BECKER" -> "BECKER, SARA"
            else -> prof
        }

        "50:965" -> when (prof) {
            "CHACON, DOMINIC" -> "CHACON, JOSE"
            "ELLIOT, KENNETH" -> "ELLIOTT, KENNETH"
            "FORBES-ERICKSO" -> "FORBES-ERICKSON, DENISE"
            "HIIBLE, MILLIE" -> "HIIBEL, MILLIE"
            else -> prof
        }

        "50:989" -> when (prof) {
            "BARBEE, YOW" -> "BARBEEYOW, GWENDOLYN"
            "BROWN, JOHN" -> "BROWN, JAMES" // probably
            else -> prof
        }

        else -> prof
    }
}