-- Function: public.checkuserhascreditcard()

DROP FUNCTION public.checkuserhascreditcard();

CREATE OR REPLACE FUNCTION public.checkuserhascreditcard()
  RETURNS trigger AS
$BODY$
BEGIN 
	IF NOT EXISTS (SELECT * FROM creditcards 
				WHERE creditcards.id = NEW.primary_credit_card
				AND creditcards.user_id = NEW.id) THEN
	            RAISE EXCEPTION 'This credit card belongs to another user';
    END IF;
    RETURN NEW;
END$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.checkuserhascreditcard()
  OWNER TO ***REMOVED***;


CREATE TRIGGER checkUserHasCreditCard 
AFTER UPDATE ON users
FOR EACH ROW EXECUTE PROCEDURE checkUserHasCreditCard();
