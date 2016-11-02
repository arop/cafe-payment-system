-- Function: public.updateNumberVouchersIssuedUser()

DROP FUNCTION public.updateNumberVouchersIssuedUser();

CREATE OR REPLACE FUNCTION public.updateNumberVouchersIssuedUser()
  RETURNS trigger AS
$BODY$
BEGIN 
	IF NEW.type = 'd' THEN
		UPDATE users 
		SET discount_vouchers_issued = discount_vouchers_issued + 1 
		WHERE id = NEW.user_id;
	END IF;
	return new;
END$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.updateNumberVouchersIssuedUser()
  OWNER TO ***REMOVED***;


CREATE TRIGGER updateNumberVouchersIssuedUserTrigger
AFTER INSERT ON vouchers
FOR EACH ROW EXECUTE PROCEDURE updateNumberVouchersIssuedUser();